package org.example.project.abfall.notification

import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import kotlinx.datetime.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.LocalTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.util.concurrent.TimeUnit

private const val WORK_NAME = "abfall_daily_reminder"

actual class AbfallNotificationScheduler actual constructor() {

    actual fun scheduleDaily() {
        val context = AndroidContextHolder.applicationContext ?: return
        val request = PeriodicWorkRequestBuilder<AbfallReminderWorker>(1, TimeUnit.DAYS)
            .setInitialDelay(initialDelayToEightAm(), TimeUnit.MILLISECONDS)
            .build()
        WorkManager.getInstance(context).enqueueUniquePeriodicWork(
            WORK_NAME,
            ExistingPeriodicWorkPolicy.UPDATE,
            request,
        )
    }

    actual fun cancel() {
        val context = AndroidContextHolder.applicationContext ?: return
        WorkManager.getInstance(context).cancelUniqueWork(WORK_NAME)
    }

    private fun initialDelayToEightAm(): Long {
        val tz = TimeZone.currentSystemDefault()
        val now: LocalDateTime = Clock.System.now().toLocalDateTime(tz)
        val eight = LocalTime(8, 0)
        val targetDate = if (now.time < eight) now.date else now.date.plus(1, DateTimeUnit.DAY)
        val target = LocalDateTime(targetDate, eight).toInstant(tz)
        val nowInstant = Clock.System.now()
        return (target - nowInstant).inWholeMilliseconds
    }
}
