package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.serial.FlipperSerialSpeed
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ktx.jre.roundPercentToString
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import flipperapp.components.bridge.synchronization.ui.generated.resources.Res
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_show_status
import flipperapp.components.bridge.synchronization.ui.generated.resources.sync_status_percent
import org.jetbrains.compose.resources.stringResource

@Composable
internal fun ComposableSynchronizationStatus(state: SynchronizationState, speed: FlipperSerialSpeed?, onOpen: () -> Unit) {
    if (state !is SynchronizationState.InProgress) return
    Column(
        Modifier.fillMaxWidth().padding(14.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text(stringResource(Res.string.sync_status_percent, state.progress.roundPercentToString()), style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
        AnimatedStatusComposable(state)
        SyncActivityIndicator(speed)
        ComposableCancelButton(stringResource(Res.string.sync_show_status), onOpen)
    }
}
