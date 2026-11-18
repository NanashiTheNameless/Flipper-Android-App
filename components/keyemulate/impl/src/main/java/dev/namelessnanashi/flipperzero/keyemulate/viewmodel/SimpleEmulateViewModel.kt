package dev.namelessnanashi.flipperzero.keyemulate.viewmodel

import android.app.Application
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.keyemulate.tasks.CloseEmulateAppTaskHolder
import dev.zacsweers.metro.Inject

class SimpleEmulateViewModel @Inject constructor(
    synchronizationApi: SynchronizationApi,
    closeEmulateAppTaskHolder: CloseEmulateAppTaskHolder,
    application: Application,
    settings: DataStore<Settings>,
    fFeatureProvider: FFeatureProvider,
    fDeviceOrchestrator: FDeviceOrchestrator
) : EmulateViewModel(
    synchronizationApi,
    closeEmulateAppTaskHolder,
    application,
    settings,
    fFeatureProvider,
    fDeviceOrchestrator
) {
    override val TAG = "SimpleEmulateViewModel"
}
