package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import android.content.Context
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.share.ShareHelper
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.model.ExportState
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject

class SettingsViewModel @Inject constructor(
    private val dataStoreSettings: DataStore<Settings>,
    private val exportKeysHelper: ExportKeysHelper,
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "SettingsViewModel"

    private val settingsStateFlow = MutableStateFlow(Settings())
    private val exportStateFlow = MutableStateFlow(ExportState.NOT_STARTED)

    init {
        dataStoreSettings.data.onEach {
            settingsStateFlow.emit(it)
        }.launchIn(viewModelScope)
    }

    fun getState() = settingsStateFlow.asStateFlow()

    fun getExportState(): StateFlow<ExportState> = exportStateFlow

    fun onSwitchDebug(value: Boolean) {
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    enabled_debug_settings = value
                )
            }
        }
    }

    fun onSwitchExperimental(value: Boolean) {
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    disabled_experimental_functions = !value
                )
            }
        }
    }

    fun onMakeExport(context: Context) {
        if (!exportStateFlow.compareAndSet(ExportState.NOT_STARTED, ExportState.IN_PROGRESS)) {
            return
        }
        viewModelScope.launch {
            val file = try {
                exportKeysHelper.createBackupArchive()
            } catch (zipException: Exception) {
                error(zipException) { "Failed create backup" }
                null
            } ?: return@launch
            withContext(Dispatchers.Main) {
                ShareHelper.shareFile(context, file, R.string.export_keys_picker_title)
                exportStateFlow.compareAndSet(ExportState.IN_PROGRESS, ExportState.NOT_STARTED)
            }
        }
    }

    fun onExpertModeActivate() {
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    expert_mode = true
                )
            }
        }
    }

    fun onSwitchForkNotice(show: Boolean) {
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    hide_fork_notice_on_start = !show
                )
            }
        }
    }

    fun onSwitchVibration(vibration: Boolean) {
        viewModelScope.launch {
            dataStoreSettings.updateData {
                it.copy(
                    disabled_vibration = !vibration
                )
            }
        }
    }
}
