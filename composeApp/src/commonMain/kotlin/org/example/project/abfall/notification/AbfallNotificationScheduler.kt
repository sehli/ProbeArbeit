package org.example.project.abfall.notification

expect class AbfallNotificationScheduler() {
    fun scheduleDaily()
    fun cancel()
}
