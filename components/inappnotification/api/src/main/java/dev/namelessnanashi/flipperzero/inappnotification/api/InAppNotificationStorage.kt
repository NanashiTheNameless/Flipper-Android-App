package dev.namelessnanashi.flipperzero.inappnotification.api

import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification

interface InAppNotificationStorage {
    fun subscribe(listener: InAppNotificationListener)
    fun unsubscribe()
    fun addNotification(notification: InAppNotification)
}
