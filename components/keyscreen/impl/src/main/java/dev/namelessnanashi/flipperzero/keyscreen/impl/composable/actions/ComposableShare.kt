package dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyscreen.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRow
import dev.namelessnanashi.flipperzero.keyscreen.impl.composable.actions.common.ComposableActionRowInProgress
import dev.namelessnanashi.flipperzero.keyscreen.model.ShareState
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableShare(shareState: ShareState, onShare: () -> Unit) {
    when (shareState) {
        ShareState.PROGRESS -> ComposableActionRowInProgress(R.string.keyscreen_share_text)
        ShareState.NOT_SHARING -> ComposableActionRow(
            iconId = DesignSystem.drawable.ic_upload,
            descriptionId = R.string.keyscreen_share_text,
            onClick = onShare
        )
    }
}
