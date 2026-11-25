package dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.FlipperPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.getDarkPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.composable.pallet.generated.toAnimatePallet

@Composable
fun getThemedFlipperPalletV2(): FlipperPalletV2 = remember { getDarkPallet() }.toAnimatePallet()
