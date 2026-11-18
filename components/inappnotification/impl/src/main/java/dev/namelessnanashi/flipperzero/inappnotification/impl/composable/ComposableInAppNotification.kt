package dev.namelessnanashi.flipperzero.inappnotification.impl.composable

import android.os.Handler
import android.os.Looper
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Card
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationError
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationHideApp
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationReadyToUpdate
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationReportApp
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationSavedKey
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationSelfUpdateError
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationSelfUpdateReady
import dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type.ComposableInAppNotificationSelfUpdateStarted
import kotlin.math.max

const val VISIBLE_ANIMATION_MS = 1000

@Composable
fun ComposableInAppNotification(
    notification: InAppNotification,
    onNotificationHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComposableInAppNotificationCard(
        notification = notification,
        onNotificationHide = onNotificationHide,
        modifier = modifier
    )
}

@Composable
@Suppress("LongMethod")
private fun ComposableInAppNotificationCard(
    notification: InAppNotification,
    onNotificationHide: () -> Unit,
    modifier: Modifier = Modifier
) {
    var visibleState by remember { mutableStateOf(false) }
    var actionClick by remember { mutableStateOf(false) }
    LaunchedEffect(notification) {
        visibleState = true
    }

    val onClickAction = remember(onNotificationHide) {
        {
            visibleState = false
            actionClick = true
            onNotificationHide()
        }
    }
    AnimatedVisibility(
        modifier = modifier,
        visible = visibleState,
        enter = fadeIn(animationSpec = tween(VISIBLE_ANIMATION_MS)),
        exit = fadeOut(animationSpec = tween(VISIBLE_ANIMATION_MS))
    ) {
        Card(
            modifier = Modifier
                .padding(all = 14.dp)
                .fillMaxWidth(),
            shape = RoundedCornerShape(size = 8.dp),
            backgroundColor = LocalPallet.current.notificationCard
        ) {
            when (notification) {
                is InAppNotification.Successful -> ComposableInAppNotificationSavedKey(notification)

                is InAppNotification.SelfUpdateReady ->
                    ComposableInAppNotificationSelfUpdateReady(notification, onClickAction)

                is InAppNotification.SelfUpdateError ->
                    ComposableInAppNotificationSelfUpdateError()

                is InAppNotification.SelfUpdateStarted ->
                    ComposableInAppNotificationSelfUpdateStarted()

                InAppNotification.ReportApp -> ComposableInAppNotificationReportApp()
                is InAppNotification.HiddenApp -> ComposableInAppNotificationHideApp(
                    notification = notification,
                    onClickAction = onClickAction
                )

                is InAppNotification.Error -> ComposableInAppNotificationError(
                    notification,
                    onClickAction
                )

                InAppNotification.ReadyToUpdateFaps -> ComposableInAppNotificationReadyToUpdate(
                    onAction = onClickAction
                )
            }
        }
    }
    DisposableEffect(notification, onNotificationHide) {
        val handler = Handler(Looper.getMainLooper())

        val fadeOutRunnable = {
            visibleState = false
        }
        val hiddenRunnable = {
            if (!actionClick) {
                onNotificationHide()
            }
        }

        handler.postDelayed(
            fadeOutRunnable,
            max(0L, notification.durationMs - VISIBLE_ANIMATION_MS)
        )
        handler.postDelayed(
            hiddenRunnable,
            notification.durationMs
        )

        onDispose {
            handler.removeCallbacks(fadeOutRunnable)
            handler.removeCallbacks(hiddenRunnable)
        }
    }
}
