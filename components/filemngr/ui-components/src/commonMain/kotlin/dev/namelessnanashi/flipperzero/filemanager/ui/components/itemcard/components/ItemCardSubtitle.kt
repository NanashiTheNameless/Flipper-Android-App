package dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
internal fun ItemCardSubtitle(
    text: String,
    isLoading: Boolean,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .placeholderConnecting(visible = isLoading),
        text = text,
        style = LocalTypography.current.subtitleM10,
        color = LocalPalletV2.current.text.label.secondary
    )
}
