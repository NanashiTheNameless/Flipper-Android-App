package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRow
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableEdit(onClick: () -> Unit) {
    ComposableActionRow(
        iconId = DesignSystem.drawable.ic_edit_icon,
        descriptionId = R.string.keyscreen_edit_text,
        onClick = onClick
    )
}
