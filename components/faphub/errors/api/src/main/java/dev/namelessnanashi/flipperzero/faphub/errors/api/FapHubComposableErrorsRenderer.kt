package dev.namelessnanashi.flipperzero.faphub.errors.api

import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError

@Immutable
interface FapHubComposableErrorsRenderer {
    @Suppress("FunctionNaming")
    fun LazyListScope.ComposableThrowableErrorListItem(
        throwable: FapHubError,
        onRetry: () -> Unit,
        modifier: Modifier,
        fapErrorSize: FapErrorSize
    )

    @Composable
    fun ComposableThrowableError(
        throwable: FapHubError,
        onRetry: () -> Unit,
        modifier: Modifier,
        fapErrorSize: FapErrorSize
    )
}
