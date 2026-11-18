package dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.getDarkPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.getLightPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.toAnimatePallet

/**
 * Please, use instead LocalPalletV2
 *
 * @return the necessary Pallet depending on the theme
 */
@Composable
fun getThemedFlipperPalletV2(isLight: Boolean): FlipperPalletV2 {
    return remember(isLight) {
        if (isLight) {
            getLightPallet()
        } else {
            getDarkPallet()
        }
    }.toAnimatePallet()
}
