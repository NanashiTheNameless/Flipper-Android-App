package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRow

@Composable
fun ComposableRestore(onClick: () -> Unit) {
    ComposableActionRow(
        iconId = R.drawable.ic_restore,
        descriptionId = R.string.keyscreen_restore_text,
        onClick = onClick
    )
}
