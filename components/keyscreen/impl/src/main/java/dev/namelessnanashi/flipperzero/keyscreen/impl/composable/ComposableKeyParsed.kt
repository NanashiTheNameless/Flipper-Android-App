package dev.namelessnanashi.flipperzero.keyscreen.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.EmulateConfig
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.keyemulate.api.KeyEmulateApi
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.ComposableDelete
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.ComposableNfcEdit
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.ComposableRestore
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.ComposableShare
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.card.ComposableKeyCard
import dev.namelessnanashi.flipperzero.keyscreen.model.DeleteState
import dev.namelessnanashi.flipperzero.keyscreen.model.KeyScreenState
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarBackIcon
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarTitleWithName
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableKeyScreenAppBar

@Composable
@Suppress("LongParameterList", "NonSkippableComposable")
fun ComposableKeyParsed(
    keyScreenState: KeyScreenState.Ready,
    synchronizationUiApi: SynchronizationUiApi,
    keyEmulateApi: KeyEmulateApi,
    componentContext: ComponentContext,
    onBack: () -> Unit,
    onShare: () -> Unit,
    onOpenNfcEditor: (FlipperKeyPath) -> Unit,
    onRestore: () -> Unit,
    onDelete: () -> Unit,
    setFavorite: (Boolean) -> Unit,
    onEdit: () -> Unit,
    emulateConfig: EmulateConfig?,
    modifier: Modifier = Modifier
) {
    val scrollState = rememberScrollState()
    Column(modifier = modifier.verticalScroll(scrollState)) {
        ComposableKeyScreenBar(keyScreenState.flipperKey.path.nameWithoutExtension, onBack = onBack)
        ComposableKeyCard(
            parsedKey = keyScreenState.parsedKey,
            deleteState = keyScreenState.deleteState,
            modifier = Modifier.padding(all = 24.dp),
            synchronizationState = if (keyScreenState.deleteState == DeleteState.NOT_DELETED) {
                { ->
                    synchronizationUiApi.RenderSynchronizationState(
                        keyPath = keyScreenState.flipperKey.getKeyPath(),
                        withText = true,
                        componentContext = componentContext
                    )
                }
            } else {
                null
            },
            favoriteState = keyScreenState.favoriteState,
            onSwitchFavorites = setFavorite,
            onEditName = onEdit,
            emulatingInProgress = keyScreenState.emulatingInProgress
        )

        if (keyScreenState.deleteState == DeleteState.NOT_DELETED) {
            emulateConfig?.let { config ->
                keyEmulateApi.ComposableEmulateButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(start = 20.dp, end = 20.dp, bottom = 14.dp),
                    emulateConfig = config,
                    isSynchronized = keyScreenState.flipperKey.synchronized,
                    componentContext = componentContext
                )
            }

            if (keyScreenState.isSupportEditing) {
                ComposableNfcEdit(
                    emulatingInProgress = keyScreenState.emulatingInProgress,
                    onClick = {
                        onOpenNfcEditor(keyScreenState.flipperKey.getKeyPath())
                    }
                )
            }
            ComposableShare(keyScreenState.shareState) {
                onShare()
            }
        } else if (keyScreenState.deleteState == DeleteState.DELETED) {
            ComposableRestore(onClick = onRestore)
        }

        ComposableDelete(keyScreenState.deleteState, onClick = onDelete)
    }
}

@Composable
private fun ComposableKeyScreenBar(keyName: String, onBack: () -> Unit) {
    ComposableKeyScreenAppBar(
        startBlock = {
            ComposableBarBackIcon(it, onBack)
        },
        centerBlock = {
            ComposableBarTitleWithName(
                modifier = it,
                titleId = R.string.keyscreen_title,
                name = keyName
            )
        }
    )
}
