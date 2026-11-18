package dev.namelessnanashi.flipperzero.bottombar.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel.InAppNotificationState
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationRenderer
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification

@Composable
fun ComposableInAppNotification(
    notificationRenderer: InAppNotificationRenderer,
    notificationState: InAppNotificationState,
    onNotificationHide: (notification: InAppNotification) -> Unit,
    modifier: Modifier = Modifier
) {
    if (notificationState !is InAppNotificationState.ShownNotification) {
        return
    }
    notificationRenderer.InAppNotification(
        notification = notificationState.notification,
        modifier = modifier
    ) {
        onNotificationHide(notificationState.notification)
    }
}
