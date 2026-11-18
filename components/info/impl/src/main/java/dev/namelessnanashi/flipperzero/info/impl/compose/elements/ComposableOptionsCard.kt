package dev.namelessnanashi.flipperzero.info.impl.compose.elements

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.shared.ButtonElementRow
import dev.namelessnanashi.flipperzero.info.shared.InfoElementCard
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableOptionsCard(
    modifier: Modifier = Modifier,
    onOpenOptions: () -> Unit
) {
    InfoElementCard(modifier) {
        ButtonElementRow(
            titleId = R.string.info_device_options,
            iconId = R.drawable.ic_options,
            color = LocalPallet.current.text80,
            onClick = onOpenOptions,
            actionIconId = DesignSystem.drawable.ic_navigate
        )
    }
}
