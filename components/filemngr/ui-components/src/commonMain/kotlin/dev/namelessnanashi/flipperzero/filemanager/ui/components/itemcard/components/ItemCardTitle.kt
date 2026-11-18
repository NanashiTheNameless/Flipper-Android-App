package dev.namelessnanashi.flipperzero.filemanager.ui.components.itemcard.components

import androidx.compose.foundation.basicMarquee
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
internal fun ItemCardTitle(text: String) {
    Text(
        text = text,
        style = LocalTypography.current.bodyM14,
        color = LocalPalletV2.current.text.body.primary,
        maxLines = 1,
        modifier = Modifier.basicMarquee(),
        overflow = TextOverflow.Clip
    )
}
