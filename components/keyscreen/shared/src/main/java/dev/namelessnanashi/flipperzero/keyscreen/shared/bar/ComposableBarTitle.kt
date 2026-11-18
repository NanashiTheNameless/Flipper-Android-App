package dev.namelessnanashi.flipperzero.keyscreen.shared.bar

import androidx.annotation.StringRes
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
fun ComposableBarTitle(text: String, modifier: Modifier = Modifier) {
    Text(
        modifier = modifier,
        text = text,
        color = LocalPallet.current.text88,
        style = LocalTypography.current.titleEB20
    )
}

@Composable
fun ComposableBarTitle(@StringRes textId: Int, modifier: Modifier = Modifier) {
    ComposableBarTitle(modifier = modifier, text = stringResource(textId))
}
