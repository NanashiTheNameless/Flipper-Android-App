package dev.namelessnanashi.flipperzero.share.receive.composable

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyScreenApi
import dev.namelessnanashi.flipperzero.share.receive.composable.screens.ComposableKeyErrorScreen
import dev.namelessnanashi.flipperzero.share.receive.composable.screens.ComposableKeyInProgressScreen
import dev.namelessnanashi.flipperzero.share.receive.composable.screens.ComposableKeySaveScreen
import dev.namelessnanashi.flipperzero.share.receive.models.ReceiveState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun ComposableKeyReceive(
    keyScreenApi: KeyScreenApi,
    state: ReceiveState,
    onSave: () -> Unit,
    onRetry: () -> Unit,
    onCancel: () -> Unit
) {
    when (state) {
        is ReceiveState.Pending -> ComposableKeySaveScreen(
            keyScreenApi = keyScreenApi,
            keyParsed = state.parsed,
            savingInProgress = state.isSaving,
            onSave = onSave,
            onCancel = onCancel
        )

        is ReceiveState.Error -> ComposableKeyErrorScreen(
            typeError = state.type,
            onCancel = onCancel,
            onRetry = onRetry
        )

        ReceiveState.NotStarted -> ComposableKeyInProgressScreen(
            keyScreenApi = keyScreenApi,
            onCancel = onCancel
        )

        ReceiveState.Finished -> LaunchedEffect(onCancel) {
            withContext(Dispatchers.Main) {
                onCancel()
            }
        }
    }
}
