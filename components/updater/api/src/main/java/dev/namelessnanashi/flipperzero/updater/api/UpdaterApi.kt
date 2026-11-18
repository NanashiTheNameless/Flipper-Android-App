package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.model.UpdatingStateWithRequest
import kotlinx.coroutines.flow.StateFlow

interface UpdaterApi {
    fun isUpdateInProcess(): Boolean
    fun getState(): StateFlow<UpdatingStateWithRequest>
    fun resetState()

    fun onDeviceConnected(versionName: FirmwareVersion)

    fun start(updateRequest: UpdateRequest)
    suspend fun cancel(silent: Boolean = false)
}
