package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialog
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.R
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel.InstallAllState

@Composable
fun ComposableInstallAll(
    state: InstallAllState,
    onRequestInstallAll: () -> Unit,
    onConfirmInstallAll: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(LocalPallet.current.accentSecond)
                .clickableRipple(onClick = onRequestInstallAll),
            contentAlignment = Alignment.Center
        ) {
            if (state == InstallAllState.InProgress) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .padding(vertical = 9.dp)
                        .size(24.dp),
                    strokeWidth = 2.dp,
                    color = LocalPallet.current.onFapHubInstallButton
                )
            } else {
                Text(
                    modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                    text = stringResource(R.string.faphub_catalog_install_all),
                    textAlign = TextAlign.Center,
                    style = LocalTypography.current.fapHubButtonText.copy(fontSize = 18.sp),
                    color = LocalPallet.current.onFapHubInstallButton
                )
            }
        }
        val resultText = when (state) {
            is InstallAllState.Queued -> pluralStringResource(
                R.plurals.faphub_catalog_install_all_queued,
                state.count,
                state.count
            )
            InstallAllState.Failed -> stringResource(R.string.faphub_catalog_install_all_failed)
            else -> null
        }
        if (resultText != null) {
            Text(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 4.dp),
                text = resultText,
                textAlign = TextAlign.Center,
                style = LocalTypography.current.subtitleR12,
                color = LocalPallet.current.text40
            )
        }
    }

    if (state == InstallAllState.ConfirmPending) {
        FlipperDialog(
            title = stringResource(R.string.faphub_catalog_install_all_dialog_title),
            text = stringResource(R.string.faphub_catalog_install_all_dialog_text),
            buttonText = stringResource(R.string.faphub_catalog_install_all_dialog_button),
            onClickButton = onConfirmInstallAll,
            onDismissRequest = onDismiss
        )
    }
}
