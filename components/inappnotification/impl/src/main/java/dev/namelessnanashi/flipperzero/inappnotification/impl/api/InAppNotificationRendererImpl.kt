package dev.namelessnanashi.flipperzero.inappnotification.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationRenderer
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.ComposableInAppNotification
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppGraph::class)
class InAppNotificationRendererImpl @Inject constructor() : InAppNotificationRenderer {
    @Composable
    override fun InAppNotification(
        notification: InAppNotification,
        modifier: Modifier,
        onNotificationHide: () -> Unit,
    ) {
        ComposableInAppNotification(
            notification = notification,
            onNotificationHide = onNotificationHide,
            modifier = modifier
        )
    }
}
