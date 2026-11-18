package dev.namelessnanashi.flipperzero.inappnotification.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification

interface InAppNotificationRenderer {
    @Composable
    fun InAppNotification(
        notification: InAppNotification,
        modifier: Modifier,
        onNotificationHide: () -> Unit
    )
}
