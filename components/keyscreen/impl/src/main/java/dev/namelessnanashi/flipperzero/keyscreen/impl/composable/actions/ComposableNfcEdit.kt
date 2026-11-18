package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRow

@Composable
fun ComposableNfcEdit(
    onClick: () -> Unit,
    emulatingInProgress: Boolean
) {
    ComposableActionRow(
        iconId = R.drawable.ic_nfc_edit_icon,
        descriptionId = R.string.keyscreen_nfc_edit_text,
        onClick = onClick,
        isActive = !emulatingInProgress
    )
}
