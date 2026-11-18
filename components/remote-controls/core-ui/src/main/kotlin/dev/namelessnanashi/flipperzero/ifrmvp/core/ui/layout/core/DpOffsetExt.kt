package dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.core

import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.unit.DpOffset
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrButton

@Composable
fun BoxWithConstraintsScope.rememberDpOffset(
    position: IfrButton.Position,
    maxRows: Int,
    maxColumns: Int,
): DpOffset {
    val calculatedWidth = this.maxWidth
    val calculatedHeight = this.maxHeight
    return remember(calculatedWidth, calculatedHeight, position) {
        DpOffset(
            calculatedWidth * (position.x.toFloat() / maxColumns),
            calculatedHeight * (position.y.toFloat() / maxRows)
        )
    }
}
