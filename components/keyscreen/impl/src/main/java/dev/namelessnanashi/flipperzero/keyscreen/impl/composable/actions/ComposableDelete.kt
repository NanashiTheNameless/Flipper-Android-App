package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRow
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRowInProgress
import dev.namelessnanashi.flipperzero.keyscreen.model.DeleteState
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableDelete(deleteState: DeleteState, onClick: () -> Unit) {
    if (deleteState == DeleteState.PROGRESS) {
        ComposableActionRowInProgress(
            descriptionId = R.string.keyscreen_deleting_text,
            descriptionColor = LocalPallet.current.keyDelete
        )
        return
    }

    val descriptionId = if (deleteState == DeleteState.DELETED) {
        R.string.keyscreen_delete_permanently_text
    } else {
        R.string.keyscreen_delete_text
    }

    ComposableActionRow(
        iconId = DesignSystem.drawable.ic_trash_icon,
        tint = LocalPallet.current.keyDelete,
        descriptionId = descriptionId,
        descriptionColor = LocalPallet.current.keyDelete,
        onClick = onClick
    )
}
