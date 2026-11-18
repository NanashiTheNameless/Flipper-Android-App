package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.composable.components

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.button.core.ButtonClickEvent
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.shared.GridPagesContent
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.composable.util.contentKey
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.RemoteGridComponent

@Composable
internal fun RemoteGridComposableContent(
    remoteGridComponent: RemoteGridComponent,
    flipperDispatchDialogApi: FlipperDispatchDialogApi,
    errorsRenderer: FapHubComposableErrorsRenderer,
    model: RemoteGridComponent.Model,
    modifier: Modifier = Modifier
) {
    AnimatedContent(
        targetState = model,
        modifier = modifier,
        transitionSpec = { fadeIn().togetherWith(fadeOut()) },
        contentKey = { it.contentKey }
    ) { animatedModel ->
        when (animatedModel) {
            is RemoteGridComponent.Model.Error -> {
                errorsRenderer.ComposableThrowableError(
                    throwable = animatedModel.throwable,
                    onRetry = remoteGridComponent::tryLoad,
                    fapErrorSize = FapErrorSize.FULLSCREEN,
                    modifier = Modifier.fillMaxSize()
                )
            }

            is RemoteGridComponent.Model.Loaded -> {
                flipperDispatchDialogApi.Render(
                    dialogType = animatedModel.flipperDialog,
                    onDismiss = remoteGridComponent::dismissDialog,
                )
                GridPagesContent(
                    pagesLayout = animatedModel.pagesLayout,
                    onButtonClick = { _, clickType, keyIdentifier ->
                        when (clickType) {
                            ButtonClickEvent.SINGLE_CLICK -> {
                                remoteGridComponent.onButtonClick(keyIdentifier)
                            }

                            ButtonClickEvent.HOLD -> {
                                remoteGridComponent.onButtonLongClick(keyIdentifier)
                            }

                            ButtonClickEvent.RELEASE -> {
                                remoteGridComponent.onButtonRelease()
                            }
                        }
                    },
                    onReload = remoteGridComponent::tryLoad,
                    emulatedKeyIdentifier = animatedModel.emulatedKey,
                    isSyncing = animatedModel.isSavingFiles,
                    isConnected = animatedModel.isConnected
                )
            }

            is RemoteGridComponent.Model.Loading -> {
                RemoteGridComposableLoadingContent()
            }
        }
    }
}
