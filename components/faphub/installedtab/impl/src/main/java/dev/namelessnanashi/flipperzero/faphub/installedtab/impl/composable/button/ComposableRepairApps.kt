package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
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
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.R
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.RepairAppsState

@Composable
fun ComposableRepairApps(
    state: RepairAppsState,
    onCheck: () -> Unit,
    onRepair: () -> Unit,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = state == RepairAppsState.Checking || state == RepairAppsState.Repairing
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, LocalPallet.current.text40, RoundedCornerShape(8.dp))
            .clickableRipple(onClick = onCheck),
        contentAlignment = Alignment.Center
    ) {
        if (isBusy) {
            CircularProgressIndicator(
                modifier = Modifier
                    .padding(vertical = 9.dp)
                    .size(24.dp),
                strokeWidth = 2.dp,
                color = LocalPallet.current.text100
            )
        } else {
            Text(
                modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
                text = stringResource(R.string.faphub_installed_repair_check),
                textAlign = TextAlign.Center,
                style = LocalTypography.current.fapHubButtonText.copy(fontSize = 18.sp),
                color = LocalPallet.current.text100
            )
        }
    }

    when (state) {
        is RepairAppsState.Checked -> ComposableRepairResultDialog(state, onRepair, onDismiss)
        is RepairAppsState.Queued -> FlipperDialog(
            text = pluralStringResource(R.plurals.faphub_installed_repair_queued, state.count, state.count),
            buttonText = stringResource(R.string.faphub_installed_repair_ok),
            onClickButton = onDismiss,
            onDismissRequest = onDismiss
        )
        RepairAppsState.Failed -> FlipperDialog(
            text = stringResource(R.string.faphub_installed_repair_failed),
            buttonText = stringResource(R.string.faphub_installed_repair_ok),
            onClickButton = onDismiss,
            onDismissRequest = onDismiss
        )
        else -> {}
    }
}

@Composable
private fun ComposableRepairResultDialog(
    state: RepairAppsState.Checked,
    onRepair: () -> Unit,
    onDismiss: () -> Unit
) {
    val report = state.report
    val summary = buildString {
        append(
            pluralStringResource(
                R.plurals.faphub_installed_repair_checked,
                report.checkedCount,
                report.checkedCount
            )
        )
        append(' ')
        append(
            if (report.broken.isEmpty()) {
                stringResource(R.string.faphub_installed_repair_none_broken)
            } else {
                pluralStringResource(
                    R.plurals.faphub_installed_repair_broken,
                    report.broken.size,
                    report.broken.size,
                    report.broken.joinToString { it.fullName }
                )
            }
        )
        if (report.uncheckable.isNotEmpty()) {
            append(' ')
            append(
                pluralStringResource(
                    R.plurals.faphub_installed_repair_uncheckable,
                    report.uncheckable.size,
                    report.uncheckable.size
                )
            )
        }
    }
    if (report.broken.isEmpty()) {
        FlipperDialog(
            title = stringResource(R.string.faphub_installed_repair_title),
            text = summary,
            buttonText = stringResource(R.string.faphub_installed_repair_ok),
            onClickButton = onDismiss,
            onDismissRequest = onDismiss
        )
    } else {
        FlipperDialog(
            title = stringResource(R.string.faphub_installed_repair_title),
            text = summary,
            buttonText = stringResource(R.string.faphub_installed_repair_button),
            onClickButton = onRepair,
            onDismissRequest = onDismiss
        )
    }
}
