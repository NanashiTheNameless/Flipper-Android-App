package dev.namelessnanashi.flipperzero.keyscreen.shared.bar

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
fun ComposableBarSimpleText(
    text: String,
    modifier: Modifier = Modifier,
    color: Color = LocalPallet.current.text40,
    onClick: (() -> Unit)? = null
) {
    var textModifier = modifier

    if (onClick != null) {
        textModifier = textModifier.clickableRipple(bounded = false, onClick = onClick)
    }

    Text(
        modifier = textModifier,
        text = text,
        color = color,
        style = LocalTypography.current.buttonM16
    )
}
