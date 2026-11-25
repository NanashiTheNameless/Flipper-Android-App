package dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.draw.clip
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
internal fun ComposableFlipperFlatButton(
    text: String,
    onClick: () -> Unit,
    textColor: Color? = null
) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 12.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(30.dp))
            .background(LocalPallet.current.background)
            .border(1.5.dp, textColor ?: LocalPallet.current.text100, RoundedCornerShape(30.dp))
            .clickableRipple(onClick = onClick)
            .padding(vertical = 14.dp),
        text = text,
        textAlign = TextAlign.Center,
        color = textColor ?: LocalPallet.current.text100,
        style = LocalTypography.current.bodyM14
    )
}
