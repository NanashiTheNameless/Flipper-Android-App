package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.material.Surface
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.mutableStateOf
import androidx.compose.ui.window.Dialog
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ktx.jre.roundPercentToString
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairProgress
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairStage
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
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isBusy = state is RepairAppsState.Checking || state is RepairAppsState.Repairing
    var showProgress by remember { mutableStateOf(true) }
    LaunchedEffect(isBusy) { if (isBusy) showProgress = true }
    Box(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .border(1.dp, LocalPallet.current.text40, RoundedCornerShape(8.dp))
            .clickableRipple(onClick = { if (isBusy) showProgress = true else onCheck() }),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 9.dp),
            text = stringResource(if (isBusy) R.string.faphub_installed_repair_show_progress else R.string.faphub_installed_repair_check),
            textAlign = TextAlign.Center,
            style = LocalTypography.current.fapHubButtonText.copy(fontSize = 18.sp),
            color = LocalPallet.current.text100
        )
    }
    if (isBusy && showProgress) {
        val progress = when (state) {
            is RepairAppsState.Checking -> state.progress
            is RepairAppsState.Repairing -> state.progress
            else -> FapRepairProgress()
        }
        ComposableRepairProgressDialog(progress, { showProgress = false }, onCancel)
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

@Composable
private fun ComposableRepairProgressDialog(progress: FapRepairProgress, onDismiss: () -> Unit, onCancel: () -> Unit) {
    Dialog(onDismissRequest = onDismiss) {
        Surface(shape = RoundedCornerShape(24.dp), color = LocalPallet.current.background) {
            Column(
                Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.faphub_installed_repair_title), style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
                CircularProgressIndicator(color = LocalPallet.current.text100, strokeWidth = 2.dp)
                Text(progress.fraction.roundPercentToString(), color = LocalPallet.current.text100)
                Text(stringResource(when (progress.stage) {
                    FapRepairStage.PREPARING -> R.string.faphub_installed_repair_preparing
                    FapRepairStage.CHECKING -> R.string.faphub_installed_repair_checking
                    FapRepairStage.DOWNLOADING -> R.string.faphub_installed_repair_downloading
                    FapRepairStage.QUEUING -> R.string.faphub_installed_repair_queuing
                }), color = LocalPallet.current.text100)
                if (progress.total > 0) Text(stringResource(R.string.faphub_installed_repair_count, progress.completed, progress.total), color = LocalPallet.current.text60)
                progress.appName?.let { Text(it, color = LocalPallet.current.text100, textAlign = TextAlign.Center) }
                if (progress.stage == FapRepairStage.DOWNLOADING) Text(stringResource(R.string.faphub_installed_repair_download_percent, progress.downloadProgress.roundPercentToString()), color = LocalPallet.current.text60)
                Text(stringResource(R.string.faphub_installed_repair_dismiss_hint), color = LocalPallet.current.text60, textAlign = TextAlign.Center)
                ComposableCancelButton(stringResource(R.string.faphub_installed_repair_dismiss), onDismiss)
                ComposableCancelButton(stringResource(R.string.faphub_installed_repair_cancel), onCancel)
            }
        }
    }
}
