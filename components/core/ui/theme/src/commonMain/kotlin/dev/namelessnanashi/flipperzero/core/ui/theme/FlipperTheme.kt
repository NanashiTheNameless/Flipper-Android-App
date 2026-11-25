package dev.namelessnanashi.flipperzero.core.ui.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.selection.LocalTextSelectionColors
import androidx.compose.material.LocalContentColor
import androidx.compose.material.MaterialTheme
import androidx.compose.material.Shapes
import androidx.compose.material.Typography
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.getOxProtoFamily
import androidx.compose.material.contentColorFor
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.compositionLocalOf
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.FlipperPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.FlipperTypography
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.getThemedFlipperPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.getTypography
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.getThemedFlipperPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.toMaterialColors
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.toTextSelectionColors
import dev.namelessnanashi.flipperzero.core.ui.theme.viewmodel.ThemeViewModel

val LocalPallet = compositionLocalOf<FlipperPallet> { error("No local pallet") }
val LocalPalletV2 = compositionLocalOf<FlipperPalletV2> { error("No local pallet") }
val LocalTypography = compositionLocalOf<FlipperTypography> { error("No local typography") }

@Suppress("UNUSED_PARAMETER")
@Composable
fun FlipperTheme(
    themeViewModel: ThemeViewModel,
    content: @Composable () -> Unit,
) {
    FlipperTheme(
        content = content,
        pallet = getThemedFlipperPallet(),
        palletV2 = getThemedFlipperPalletV2()
    )
}

@Composable
private fun FlipperTheme(
    pallet: FlipperPallet,
    palletV2: FlipperPalletV2,
    content: @Composable () -> Unit
) {
    val colors = pallet.toMaterialColors()
    val shapes = Shapes(medium = RoundedCornerShape(size = 10.dp))

    MaterialTheme(
        shapes = shapes,
        colors = colors,
        // Fork: plain Text without an app style uses 0xProto too
        typography = Typography(defaultFontFamily = getOxProtoFamily())
    ) {
        CompositionLocalProvider(
            LocalPallet provides pallet,
            LocalPalletV2 provides palletV2,
            LocalTypography provides getTypography(),
            LocalContentColor provides colors.contentColorFor(backgroundColor = pallet.background),
            LocalTextSelectionColors provides pallet.toTextSelectionColors(),
            content = content
        )
    }
}

// For preview composable function, because we don`t provide LocalPallet/Typography/MaterialDesign
@Composable
fun FlipperThemeInternal(
    content: @Composable () -> Unit
) {
    FlipperTheme(
        pallet = getThemedFlipperPallet(),
        palletV2 = getThemedFlipperPalletV2(),
        content = content
    )
}
