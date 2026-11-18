package dev.namelessnanashi.flipperzero.inappnotification.impl.composable.type

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.namelessnanashi.flipperzero.inappnotification.impl.R

@Composable
fun ComposableInAppNotificationHideApp(
    notification: InAppNotification.HiddenApp,
    onClickAction: () -> Unit,
    modifier: Modifier = Modifier
) {
    ComposableInAppNotificationBase(
        modifier = modifier,
        icon = {
            ComposableSaveIcon()
        },
        titleId = R.string.hide_app_title,
        descId = R.string.hide_app_desc,
        actionButton = {
            ComposableInAppNotificationBaseActionText(
                titleId = R.string.hide_app_btn,
                onClick = {
                    notification.action.invoke()
                    onClickAction()
                }
            )
        }
    )
}
