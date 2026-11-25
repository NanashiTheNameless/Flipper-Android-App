package dev.namelessnanashi.flipperzero.core.ui.ktx.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

/**
 * Fork: one look for every Cancel / Stop action on a screen (they were plain text links):
 * a full-width outlined pill next to the filled [ComposableFlipperButton].
 */
@Composable
fun ComposableCancelButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    val shape = RoundedCornerShape(30.dp)
    val color = if (enabled) LocalPallet.current.text100 else LocalPallet.current.text30
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(shape)
            .background(LocalPallet.current.background)
            .border(width = 1.5.dp, color = color, shape = shape)
            .clickableRipple(enabled = enabled, onClick = onClick)
            .padding(vertical = 14.dp, horizontal = 24.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = text,
            style = LocalTypography.current.buttonB16,
            color = color,
            textAlign = TextAlign.Center
        )
    }
}
