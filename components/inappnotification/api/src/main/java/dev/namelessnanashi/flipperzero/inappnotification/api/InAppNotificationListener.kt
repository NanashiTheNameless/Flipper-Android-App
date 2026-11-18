package dev.namelessnanashi.flipperzero.inappnotification.api

import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification

@FunctionalInterface
interface InAppNotificationListener {
    suspend fun onNewNotification(notification: InAppNotification)
}
