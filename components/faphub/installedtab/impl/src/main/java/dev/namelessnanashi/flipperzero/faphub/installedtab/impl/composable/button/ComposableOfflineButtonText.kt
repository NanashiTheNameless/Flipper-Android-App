package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.height
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.R

@Composable
fun ComposableOfflineButtonText(modifier: Modifier = Modifier) {
    Box(
        modifier
            .height(38.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier,
            color = LocalPallet.current.text40,
            textAlign = TextAlign.Center,
            text = stringResource(R.string.faphub_installed_offline),
            style = LocalTypography.current.subtitleR12
        )
    }
}
