package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.buttons

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.R
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.elements.ComposableFlipperButton

@Composable
internal fun ComposableFapOpenButton(
    fapButtonSize: FapButtonSize,
    modifier: Modifier = Modifier,
    onClick: () -> Unit,
) {
    ComposableFlipperButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installation_open),
        color = LocalPallet.current.accentSecond,
        fapButtonSize = fapButtonSize,
        onClick = onClick
    )
}
