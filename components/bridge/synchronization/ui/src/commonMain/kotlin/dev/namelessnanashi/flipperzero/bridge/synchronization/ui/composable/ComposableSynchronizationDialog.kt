package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FlipperSerialSpeed
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ktx.jre.roundPercentToString
import dev.namelessnanashi.flipperzero.core.ktx.jre.toFormattedSize
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import flipperapp.components.bridge.synchronization.ui.generated.resources.Res
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_dismiss
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_dismiss_hint
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_favorites
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_hashes
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_preparing
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_status_waiting
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_status_working
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_sync_cancel
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_sync_progress
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_syncing
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_updating
import org.jetbrains.compose.resources.stringResource

@Composable
private fun AnimatedProgressComposable(progress: Float) {
    Box(
        modifier = Modifier.size(64.dp),
        contentAlignment = Alignment.Center
    ) {
        val progressAnimated by animateFloatAsState(targetValue = progress)
        CircularProgressIndicator(
            modifier = Modifier.fillMaxSize(),
            color = LocalPalletV2.current.action.blue.background.primary.default,
            backgroundColor = LocalPalletV2.current.action.neutral.background.tertiary.default,
            strokeWidth = 2.dp,
            progress = progressAnimated
        )
        Text(
            modifier = Modifier,
            text = progressAnimated.roundPercentToString(),
            style = LocalTypography.current.bodyM14,
            color = LocalPalletV2.current.text.body.secondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
internal fun AnimatedStatusComposable(inProgressState: SynchronizationState.InProgress) {
    AnimatedContent(
        targetState = inProgressState,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        contentKey = {
            when (it) {
                is SynchronizationState.InProgress.Default -> 0
                is SynchronizationState.InProgress.Favorites -> 1
                is SynchronizationState.InProgress.Prepare -> 2
                is SynchronizationState.InProgress.PrepareHashes -> it.keyType.name
                is SynchronizationState.InProgress.FileInProgress -> it.fileName
            }
        },
    ) { animatedInProgressState ->
        Text(
            modifier = Modifier.fillMaxWidth(),
            text = when (animatedInProgressState) {
                is SynchronizationState.InProgress.FileInProgress -> {
                    stringResource(
                        Res.string.sync_updating,
                        animatedInProgressState.fileName
                    )
                }

                is SynchronizationState.InProgress.Default -> {
                    stringResource(Res.string.sync_sync_progress)
                }

                is SynchronizationState.InProgress.Favorites -> {
                    stringResource(Res.string.sync_favorites)
                }

                is SynchronizationState.InProgress.Prepare -> {
                    stringResource(Res.string.sync_preparing)
                }

                is SynchronizationState.InProgress.PrepareHashes -> {
                    stringResource(
                        Res.string.sync_hashes,
                        animatedInProgressState.keyType.humanReadableName
                    )
                }
            },
            style = LocalTypography.current.bodyM14,
            color = LocalPalletV2.current.text.body.secondary,
            textAlign = TextAlign.Center
        )
    }
}

/**
 * Fork: a throbber and a status line, so it is visible whether data is moving or the app is
 * waiting for the Flipper (e.g. while it hashes files on its SD card). Based on the link speed,
 * which drops to 0 a second after the last byte.
 */
@Composable
internal fun SyncActivityIndicator(speed: FlipperSerialSpeed?) {
    val bytesPerSecond = speed?.let { it.receiveBytesInSec + it.transmitBytesInSec } ?: 0L
    val isWorking = bytesPerSecond > 0L
    val color = if (isWorking) LocalPallet.current.text100 else LocalPallet.current.text40
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        CircularProgressIndicator(
            modifier = Modifier.size(14.dp),
            color = color,
            strokeWidth = 2.dp
        )
        Spacer(Modifier.width(10.dp))
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(CircleShape)
                .then(
                    if (isWorking) {
                        Modifier.background(color)
                    } else {
                        Modifier.border(1.5.dp, color, CircleShape)
                    }
                )
        )
        Spacer(Modifier.width(6.dp))
        Text(
            text = if (isWorking) {
                stringResource(Res.string.sync_status_working, bytesPerSecond.toFormattedSize())
            } else {
                stringResource(Res.string.sync_status_waiting)
            },
            style = LocalTypography.current.bodyM14,
            color = color
        )
    }
}

@Composable
private fun SynchronizationProgressContent(
    inProgressState: SynchronizationState.InProgress,
    onCancel: () -> Unit,
    onDismiss: () -> Unit,
    speed: FlipperSerialSpeed?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier,
            text = stringResource(
                Res.string.sync_syncing,
            ),
            style = LocalTypography.current.titleB18,
            color = LocalPallet.current.text60,
            textAlign = TextAlign.Center
        )

        Spacer(Modifier.height(24.dp))

        AnimatedProgressComposable(inProgressState.progress)
        Spacer(Modifier.height(8.dp))
        AnimatedStatusComposable(inProgressState)
        Spacer(Modifier.height(8.dp))
        SyncActivityIndicator(speed)

        Spacer(Modifier.height(24.dp))

        Text(
            text = stringResource(Res.string.sync_dismiss_hint),
            style = LocalTypography.current.bodyM14,
            color = LocalPallet.current.text60,
            textAlign = TextAlign.Center
        )
        Spacer(Modifier.height(16.dp))
        ComposableCancelButton(
            text = stringResource(Res.string.sync_dismiss),
            onClick = onDismiss
        )
        Spacer(Modifier.height(8.dp))
        ComposableCancelButton(
            text = stringResource(Res.string.sync_sync_cancel),
            onClick = onCancel
        )
    }
}

@Composable
internal fun ComposableSynchronizationDialog(
    inProgressState: SynchronizationState.InProgress,
    speed: FlipperSerialSpeed?,
    onDismiss: () -> Unit,
    onCancel: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(
            shape = RoundedCornerShape(24.dp),
            color = LocalPallet.current.background
        ) {
            SynchronizationProgressContent(inProgressState, onCancel, onDismiss, speed)
        }
    }
}
