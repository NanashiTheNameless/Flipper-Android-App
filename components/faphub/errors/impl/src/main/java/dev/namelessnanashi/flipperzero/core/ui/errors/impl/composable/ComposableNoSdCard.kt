package dev.namelessnanashi.flipperzero.core.ui.errors.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.faphub.errors.impl.R
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
internal fun ComposableNoSdCard(
    fapErrorSize: FapErrorSize,
    modifier: Modifier = Modifier,
    onRetry: () -> Unit
) {
    ComposableBaseError(
        modifier = modifier,
        titleId = R.string.common_error_no_sd_card_title,
        descriptionId = R.string.common_error_no_sd_card_desc,
        iconId = DesignSystem.drawable.ic_no_sd,
        darkIconId = DesignSystem.drawable.ic_no_sd_dark,
        onRetry = onRetry,
        fapErrorSize = fapErrorSize
    )
}
