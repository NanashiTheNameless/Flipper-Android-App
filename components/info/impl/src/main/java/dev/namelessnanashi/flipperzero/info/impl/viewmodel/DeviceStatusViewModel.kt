package dev.namelessnanashi.flipperzero.info.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGattInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGattInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceStatus
import dev.namelessnanashi.flipperzero.updater.api.FirmwareVersionBuilderApi
import dev.namelessnanashi.flipperzero.updater.api.UpdaterApi
import dev.namelessnanashi.flipperzero.updater.model.FlipperUpdateState
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import dev.zacsweers.metro.Inject

class DeviceStatusViewModel @Inject constructor(
    dataStorePair: DataStore<PairSettings>,
    fFeatureProvider: FFeatureProvider,
    fDeviceOrchestrator: FDeviceOrchestrator,
    devicePersistedStorage: FDevicePersistedStorage,
    private val updaterApi: UpdaterApi,
    private val firmwareVersionBuilderApi: FirmwareVersionBuilderApi
) : DecomposeViewModel() {

    // Fork: name the Flipper reported over RPC while connected (USB-only setups store none)
    @Volatile
    private var lastReportedName: String? = null

    private val deviceState = combine(
        flow = fDeviceOrchestrator.getState(),
        flow2 = fFeatureProvider.get<FGattInfoFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FGattInfoFeatureApi> }
            .flatMapLatest { status -> status?.featureApi?.getGattInfoFlow() ?: flowOf(FGattInformation()) },
        flow3 = dataStorePair.data,
        flow4 = devicePersistedStorage.getCurrentDevice()
    ) { connectionState, flipperInformation, pairSettings, selectedDevice ->
        when (connectionState) {
            is FDeviceConnectStatus.Disconnected -> {
                val knownDevice = selectedDevice ?: connectionState.device
                if (pairSettings.device_name.isBlank() && knownDevice != null) {
                    // Fork: a USB-only setup stores no name, but the device is still known
                    // (plugged in, disconnected): show it, so Connect reconnects instead of
                    // opening Bluetooth pairing
                    DeviceStatus.NoDeviceInformation(
                        lastReportedName ?: knownDevice.humanReadableName,
                        connectInProgress = false
                    )
                } else if (pairSettings.device_name.isBlank() || pairSettings.device_id.isBlank()) {
                    DeviceStatus.NoDevice
                } else {
                    DeviceStatus.NoDeviceInformation(
                        pairSettings.device_name,
                        connectInProgress = false
                    )
                }
            }

            is FDeviceConnectStatus.Connecting,
            is FDeviceConnectStatus.Disconnecting,
            is FDeviceConnectStatus.Connected -> {
                var deviceName = pairSettings.device_name
                if (deviceName.isBlank()) {
                    // Fork: set up over USB, never paired; the Flipper reports its name over RPC
                    deviceName = flipperInformation.deviceName?.takeIf { it.isNotBlank() } ?: "Unknown"
                    flipperInformation.deviceName?.takeIf { it.isNotBlank() }?.let { lastReportedName = it }
                }
                val batteryLevel = flipperInformation.batteryLevel
                if (batteryLevel == null) {
                    DeviceStatus.NoDeviceInformation(
                        deviceName,
                        connectInProgress = true
                    )
                } else {
                    DeviceStatus.Connected(
                        deviceName,
                        batteryLevel,
                        flipperInformation.isCharging
                    )
                }
            }
        }
    }.stateIn(viewModelScope, SharingStarted.Eagerly, DeviceStatus.NoDevice)

    fun getState(): StateFlow<DeviceStatus> = deviceState

    private val updateStatus = combine(
        flow = updaterApi.getState(),
        flow2 = fFeatureProvider.get<FGattInfoFeatureApi>()
            .map { it as? FFeatureStatus.Supported<FGattInfoFeatureApi> }
            .flatMapLatest { it?.featureApi?.getGattInfoFlow() ?: flowOf(null) }
            .map { gattInfo -> gattInfo?.softwareVersion }
            .map { firmwareVersion ->
                firmwareVersion?.let(firmwareVersionBuilderApi::buildFirmwareVersionFromString)
            },
        flow3 = fDeviceOrchestrator.getState(),
        transform = { updaterState, firmwareVersion, orchestratorState ->
            when (orchestratorState) {
                is FDeviceConnectStatus.Connected -> {
                    if (firmwareVersion != null) {
                        when (updaterState.state) {
                            is UpdatingState.Rebooting -> {
                                updaterApi.onDeviceConnected(firmwareVersion)
                                FlipperUpdateState.Ready
                            }

                            is UpdatingState.Complete -> {
                                FlipperUpdateState.Complete(updaterState.request?.updateTo)
                            }

                            is UpdatingState.Failed -> {
                                FlipperUpdateState.Failed(updaterState.request?.updateTo)
                            }

                            else -> FlipperUpdateState.Ready
                        }
                    } else if (updaterState.state is UpdatingState.Rebooting) {
                        FlipperUpdateState.Updating
                    } else {
                        FlipperUpdateState.ConnectingInProgress
                    }
                }

                is FDeviceConnectStatus.Connecting -> FlipperUpdateState.ConnectingInProgress
                is FDeviceConnectStatus.Disconnecting -> FlipperUpdateState.ConnectingInProgress
                is FDeviceConnectStatus.Disconnected -> FlipperUpdateState.NotConnected
            }
        }
    ).stateIn(viewModelScope, SharingStarted.Eagerly, FlipperUpdateState.NotConnected)

    fun getUpdateState(): StateFlow<FlipperUpdateState> = updateStatus
}
