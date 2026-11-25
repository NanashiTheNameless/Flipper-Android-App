package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairApi
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairReport
import dev.zacsweers.metro.Inject
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairProgress
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RepairAppsState {
    data object Idle : RepairAppsState
    data class Checking(val progress: FapRepairProgress = FapRepairProgress()) : RepairAppsState
    data class Checked(val report: FapRepairReport) : RepairAppsState
    data class Repairing(val progress: FapRepairProgress = FapRepairProgress()) : RepairAppsState
    data class Queued(val count: Int) : RepairAppsState
    data object Failed : RepairAppsState
}

class RepairAppsViewModel @Inject constructor(
    private val fapRepairApi: FapRepairApi
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "RepairAppsViewModel"

    private var workJob: Job? = null
    private val stateFlow = MutableStateFlow<RepairAppsState>(RepairAppsState.Idle)

    fun getState() = stateFlow.asStateFlow()

    fun onCheck() {
        if (stateFlow.value.isBusy()) return
        stateFlow.value = RepairAppsState.Checking()
        workJob = viewModelScope.launch {
            stateFlow.value = runCatching { fapRepairApi.check { stateFlow.value = RepairAppsState.Checking(it) } }.fold(
                onSuccess = { report -> RepairAppsState.Checked(report) },
                onFailure = { throwable ->
                    if (throwable is CancellationException) throw throwable
                    error(throwable) { "Failed to check installed apps" }
                    RepairAppsState.Failed
                }
            )
        }
    }

    fun onRepair() {
        val checked = stateFlow.value as? RepairAppsState.Checked ?: return
        stateFlow.value = RepairAppsState.Repairing()
        workJob = viewModelScope.launch {
            stateFlow.value = runCatching { fapRepairApi.repair(checked.report.broken) { stateFlow.value = RepairAppsState.Repairing(it) } }.fold(
                onSuccess = { count -> RepairAppsState.Queued(count) },
                onFailure = { throwable ->
                    if (throwable is CancellationException) throw throwable
                    error(throwable) { "Failed to queue repairs" }
                    RepairAppsState.Failed
                }
            )
        }
    }

    fun onCancel() {
        workJob?.cancel()
        stateFlow.value = RepairAppsState.Idle
    }

    fun onDismiss() {
        if (stateFlow.value.isBusy()) return
        stateFlow.value = RepairAppsState.Idle
    }

    private fun RepairAppsState.isBusy() =
        this is RepairAppsState.Checking || this is RepairAppsState.Repairing
}
