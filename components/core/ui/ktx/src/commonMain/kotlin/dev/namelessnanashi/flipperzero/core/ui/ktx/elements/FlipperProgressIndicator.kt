package dev.namelessnanashi.flipperzero.core.ui.ktx.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import kotlin.math.roundToInt

private const val PERCENT_MAX = 100

@Suppress("UNUSED_PARAMETER")
@Composable
fun FlipperProgressIndicator(
    accentColor: Color,
    secondColor: Color,
    painter: Painter?,
    percent: Float?,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .defaultMinSize(minHeight = 46.dp)
            .clip(RoundedCornerShape(9.dp))
            .background(LocalPallet.current.background)
            .border(3.dp, accentColor, RoundedCornerShape(9.dp)),
        contentAlignment = Alignment.CenterStart
    ) {
        if (painter != null) {
            Icon(
                modifier = Modifier
                    .padding(8.dp)
                    .size(28.dp),
                painter = painter,
                contentDescription = null,
                tint = accentColor
            )
        }

        val progressText = if (percent != null) {
            "${(percent * PERCENT_MAX).roundToInt()}%"
        } else {
            animatedDots()
        }

        Text(
            modifier = Modifier
                .padding(top = 5.dp, bottom = 6.dp)
                .fillMaxWidth(),
            text = progressText,
            textAlign = TextAlign.Center,
            style = LocalTypography.current.updateText40,
            color = accentColor
        )
    }
}
