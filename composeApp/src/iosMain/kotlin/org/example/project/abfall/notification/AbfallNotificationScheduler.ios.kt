package org.example.project.abfall.notification

actual class AbfallNotificationScheduler actual constructor() {
    actual fun scheduleDaily() {
        // TODO iOS: BGTaskScheduler + UNUserNotificationCenter
    }

    actual fun cancel() {
        // TODO iOS
    }
}
