package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.composable.categories

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError

@Suppress("FunctionNaming")
internal fun LazyListScope.ComposableFullScreenError(
    fapHubError: FapHubError,
    errorsRenderer: FapHubComposableErrorsRenderer,
    onRetry: () -> Unit,
    modifier: Modifier = Modifier
) {
    with(errorsRenderer) {
        ComposableThrowableErrorListItem(
            modifier = modifier,
            throwable = fapHubError,
            onRetry = onRetry,
            fapErrorSize = FapErrorSize.FULLSCREEN
        )
    }
}
