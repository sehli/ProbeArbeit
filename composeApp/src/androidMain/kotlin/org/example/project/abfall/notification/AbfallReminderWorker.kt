package org.example.project.abfall.notification

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import kotlinx.datetime.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.todayIn
import org.example.project.abfall.di.ServiceLocator
import org.example.project.abfall.model.WatchedAddress

private const val CHANNEL_ID = "abfall_reminder"
private const val CHANNEL_NAME = "Abfall-Erinnerungen"

class AbfallReminderWorker(
    context: Context,
    params: WorkerParameters,
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result {
        val watched = ServiceLocator.watchedAddressRepository.all()
        if (watched.isEmpty()) return Result.success()

        ensureChannel()

        val today = Clock.System.todayIn(TimeZone.currentSystemDefault()).toString()
        var notificationId = 1000

        for (item in watched) {
            val fraktionen = runCatching { fetchFraktionenForToday(item, today) }.getOrNull()
                ?: continue
            if (fraktionen.isEmpty()) continue
            postNotification(notificationId++, item, fraktionen)
        }
        return Result.success()
    }

    private suspend fun fetchFraktionenForToday(item: WatchedAddress, today: String): List<String> {
        val termine = if (item.isHausnummer && item.hausnummerId != null) {
            ServiceLocator.repository.loadTermine(item.region, item.hausnummerId)
        } else if (item.strasseId != null) {
            ServiceLocator.repository.loadTermineForStrasse(item.region, item.strasseId)
        } else emptyList()
        return termine.filter { it.datum == today }.map { it.fraktionName }.distinct()
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val mgr = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            if (mgr.getNotificationChannel(CHANNEL_ID) == null) {
                mgr.createNotificationChannel(
                    NotificationChannel(
                        CHANNEL_ID,
                        CHANNEL_NAME,
                        NotificationManager.IMPORTANCE_DEFAULT,
                    )
                )
            }
        }
    }

    private fun postNotification(id: Int, item: WatchedAddress, fraktionen: List<String>) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ContextCompat.checkSelfPermission(
                applicationContext, Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }
        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_menu_my_calendar)
            .setContentTitle("Abfuhr heute: ${fraktionen.joinToString(", ")}")
            .setContentText(item.displayName)
            .setStyle(NotificationCompat.BigTextStyle().bigText(item.displayName))
            .setAutoCancel(true)
            .build()
        NotificationManagerCompat.from(applicationContext).notify(id, notification)
    }
}
