package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.R

@Composable
fun ComposableCancelAllButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true
) {
    ComposableCancelButton(
        modifier = modifier,
        text = stringResource(R.string.faphub_installed_update_cancel),
        onClick = onClick,
        enabled = enabled
    )
}
