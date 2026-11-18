package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapInstallationAllApi
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

sealed interface InstallAllState {
    data object Idle : InstallAllState
    data object ConfirmPending : InstallAllState
    data object InProgress : InstallAllState
    data class Queued(val count: Int) : InstallAllState
    data object Failed : InstallAllState
}

class InstallAllViewModel @Inject constructor(
    private val fapInstallationAllApi: FapInstallationAllApi
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "InstallAllViewModel"

    private val stateFlow = MutableStateFlow<InstallAllState>(InstallAllState.Idle)

    fun getState() = stateFlow.asStateFlow()

    fun onRequestInstallAll() {
        if (stateFlow.value == InstallAllState.InProgress) return
        stateFlow.value = InstallAllState.ConfirmPending
    }

    fun onDismiss() {
        if (stateFlow.value == InstallAllState.InProgress) return
        stateFlow.value = InstallAllState.Idle
    }

    fun onConfirmInstallAll() {
        stateFlow.value = InstallAllState.InProgress
        viewModelScope.launch {
            stateFlow.value = runCatching { fapInstallationAllApi.installAll() }.fold(
                onSuccess = { count -> InstallAllState.Queued(count) },
                onFailure = { throwable ->
                    error(throwable) { "Failed to queue all apps" }
                    InstallAllState.Failed
                }
            )
        }
    }
}
