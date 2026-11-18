package dev.namelessnanashi.flipperzero.infrared.impl.composable.components

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.infrared.impl.R
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarBackIcon
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarTitleWithName
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableKeyScreenAppBar

@Composable
internal fun ComposableInfraredAppBar(
    onBack: () -> Unit,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onShare: () -> Unit,
    keyName: String,
    emulatingInProgress: Boolean
) {
    ComposableKeyScreenAppBar(
        startBlock = {
            ComposableBarBackIcon(it, onBack)
        },
        centerBlock = {
            ComposableBarTitleWithName(
                modifier = it,
                titleId = R.string.infrared_title,
                name = keyName
            )
        },
        endBlock = {
            ComposableInfraredDropDown(
                modifier = it,
                onEdit = onEdit,
                onDelete = onDelete,
                onShare = onShare,
                emulatingInProgress = emulatingInProgress
            )
        }
    )
}

@Preview
@Composable
private fun PreviewInfraredAppBar() {
    FlipperThemeInternal {
        ComposableInfraredAppBar(
            onBack = {},
            onEdit = {},
            onDelete = {},
            onShare = {},
            keyName = "Test",
            emulatingInProgress = false
        )
    }
}
