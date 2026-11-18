package dev.namelessnanashi.flipperzero.faphub.uninstallbutton.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort

@Immutable
interface FapUninstallApi {
    @Composable
    fun ComposableFapUninstallButton(
        modifier: Modifier,
        applicationUid: String,
        dialogAppBox: @Composable (Modifier) -> Unit
    )

    @Composable
    fun ComposableFapUninstallButton(
        modifier: Modifier,
        fapItem: FapItem
    )

    @Composable
    fun ComposableFapUninstallButton(
        modifier: Modifier,
        fapItem: FapItemShort
    )
}
