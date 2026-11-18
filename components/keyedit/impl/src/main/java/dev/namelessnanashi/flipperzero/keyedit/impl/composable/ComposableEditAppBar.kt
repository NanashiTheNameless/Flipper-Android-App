package dev.namelessnanashi.flipperzero.keyedit.impl.composable

import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.keyedit.impl.R
import dev.namelessnanashi.flipperzero.keyedit.impl.model.SaveButtonState
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarSimpleText
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarTitle
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableKeyScreenAppBar

@Composable
fun ComposableEditAppBar(
    title: String?,
    saveButtonState: SaveButtonState,
    modifier: Modifier = Modifier,
    onBack: () -> Unit = {},
    onSave: () -> Unit = {}
) {
    ComposableKeyScreenAppBar(
        modifier = modifier,
        startBlock = {
            ComposableBarSimpleText(
                modifier = it,
                text = stringResource(id = R.string.keyedit_bar_cancel),
                onClick = onBack
            )
        },
        centerBlock = {
            ComposableBarTitle(
                modifier = it,
                text = title ?: stringResource(R.string.keyedit_bar_title)
            )
        },
        endBlock = {
            when (saveButtonState) {
                SaveButtonState.ENABLED -> ComposableBarSimpleText(
                    modifier = it,
                    text = stringResource(id = R.string.keyedit_bar_save),
                    color = LocalPallet.current.accentSecond,
                    onClick = onSave
                )
                SaveButtonState.DISABLED -> ComposableBarSimpleText(
                    modifier = it,
                    text = stringResource(id = R.string.keyedit_bar_save)
                )
                SaveButtonState.IN_PROGRESS -> CircularProgressIndicator()
            }
        }
    )
}
