package dev.namelessnanashi.flipperzero.core.ui.errors.impl.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.errors.impl.ComposableThrowableErrorInternal
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapHubComposableErrorsRenderer>())
class FapHubComposableErrorsRendererImpl @Inject constructor() : FapHubComposableErrorsRenderer {

    override fun LazyListScope.ComposableThrowableErrorListItem(
        throwable: FapHubError,
        onRetry: () -> Unit,
        modifier: Modifier,
        fapErrorSize: FapErrorSize
    ) {
        item {
            var throwableModifier = modifier

            if (fapErrorSize == FapErrorSize.FULLSCREEN) {
                throwableModifier = throwableModifier.fillParentMaxHeight()
            }

            ComposableThrowableError(
                throwable = throwable,
                onRetry = onRetry,
                modifier = throwableModifier,
                fapErrorSize = fapErrorSize
            )
        }
    }

    @Composable
    override fun ComposableThrowableError(
        throwable: FapHubError,
        onRetry: () -> Unit,
        modifier: Modifier,
        fapErrorSize: FapErrorSize
    ) {
        val deeplinkHandler = LocalDeeplinkHandler.current
        ComposableThrowableErrorInternal(
            throwable = throwable,
            onRetry = onRetry,
            modifier = modifier,
            fapErrorSize = fapErrorSize,
            onOpenDeviceScreen = {
                deeplinkHandler.handleDeeplink(Deeplink.BottomBar.OpenTab(DeeplinkBottomBarTab.DEVICE))
            }
        )
    }
}
