package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapBatchUpdateButtonState

@Composable
fun ComposableUpdateAllButton(
    state: FapBatchUpdateButtonState,
    onUpdateAll: () -> Unit,
    onCancelAll: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (state) {
        FapBatchUpdateButtonState.Offline -> ComposableOfflineButtonText(modifier)

        FapBatchUpdateButtonState.NoUpdates -> {}

        FapBatchUpdateButtonState.Loading -> ComposableCancelAllButton(
            modifier.placeholderConnecting()
        )

        is FapBatchUpdateButtonState.ReadyToUpdate ->
            ComposableUpdateAllButtonPending(
                pendingCount = state.count,
                modifier = modifier.clickableRipple(onClick = onUpdateAll)
            )

        FapBatchUpdateButtonState.UpdatingInProgress ->
            ComposableCancelAllButton(
                modifier = modifier.clickableRipple(onClick = onCancelAll)
            )
    }
}
