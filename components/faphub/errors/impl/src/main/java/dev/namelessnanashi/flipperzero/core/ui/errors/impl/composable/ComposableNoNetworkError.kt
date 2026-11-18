package dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.faphub.errors.impl.R
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
internal fun ComposableNoNetworkError(
    fapErrorSize: FapErrorSize,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ComposableBaseError(
        modifier = modifier,
        titleId = R.string.common_error_no_network_title,
        descriptionId = R.string.common_error_no_network_desc,
        iconId = DesignSystem.drawable.ic_no_internet,
        darkIconId = DesignSystem.drawable.ic_no_internet_dark,
        onRetry = onRetry,
        fapErrorSize = fapErrorSize
    )
}

@Preview(
    showBackground = true
)
@Composable
private fun ComposableNoNetworkErrorPreview() {
    FlipperThemeInternal {
        ComposableNoNetworkError(onRetry = {}, fapErrorSize = FapErrorSize.FULLSCREEN)
    }
}
