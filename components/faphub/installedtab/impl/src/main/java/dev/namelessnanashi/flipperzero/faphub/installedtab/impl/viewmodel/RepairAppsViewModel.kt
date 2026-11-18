package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairApi
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairReport
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface RepairAppsState {
    data object Idle : RepairAppsState
    data object Checking : RepairAppsState
    data class Checked(val report: FapRepairReport) : RepairAppsState
    data object Repairing : RepairAppsState
    data class Queued(val count: Int) : RepairAppsState
    data object Failed : RepairAppsState
}

class RepairAppsViewModel @Inject constructor(
    private val fapRepairApi: FapRepairApi
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "RepairAppsViewModel"

    private val stateFlow = MutableStateFlow<RepairAppsState>(RepairAppsState.Idle)

    fun getState() = stateFlow.asStateFlow()

    fun onCheck() {
        if (stateFlow.value.isBusy()) return
        stateFlow.value = RepairAppsState.Checking
        viewModelScope.launch {
            stateFlow.value = runCatching { fapRepairApi.check() }.fold(
                onSuccess = { report -> RepairAppsState.Checked(report) },
                onFailure = { throwable ->
                    error(throwable) { "Failed to check installed apps" }
                    RepairAppsState.Failed
                }
            )
        }
    }

    fun onRepair() {
        val checked = stateFlow.value as? RepairAppsState.Checked ?: return
        stateFlow.value = RepairAppsState.Repairing
        viewModelScope.launch {
            stateFlow.value = runCatching { fapRepairApi.repair(checked.report.broken) }.fold(
                onSuccess = { count -> RepairAppsState.Queued(count) },
                onFailure = { throwable ->
                    error(throwable) { "Failed to queue repairs" }
                    RepairAppsState.Failed
                }
            )
        }
    }

    fun onDismiss() {
        if (stateFlow.value.isBusy()) return
        stateFlow.value = RepairAppsState.Idle
    }

    private fun RepairAppsState.isBusy() =
        this == RepairAppsState.Checking || this == RepairAppsState.Repairing
}
