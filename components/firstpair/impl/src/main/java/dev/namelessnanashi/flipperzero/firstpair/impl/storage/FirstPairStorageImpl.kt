package dev.namelessnanashi.flipperzero.firstpair.impl.storage

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.api.utils.Constants
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle
import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.first
import java.util.UUID
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FirstPairStorage>())
class FirstPairStorageImpl @Inject constructor(
    private val pairSettingsStore: DataStore<PairSettings>,
    private val persistedStorage: FDevicePersistedStorage
) : FirstPairStorage, LogTagProvider {
    override val TAG = "FirstPairStorage"

    override fun isDeviceSelected(): Boolean {
        return runBlockingWithLog("is_device_selected") { pairSettingsStore.data.first() }.pair_device_pass
    }

    override fun markDeviceSelected(
        deviceId: String?,
        deviceName: String?,
        address: String?
    ): Unit = runBlockingWithLog("mark_device_selected") {
        pairSettingsStore.updateData {
            var pairSetting = it.copy(pair_device_pass = true)

            if (deviceId != null) {
                pairSetting = pairSetting.copy(device_id = deviceId)
            }
            if (deviceName != null) {
                var deviceNameFormatted = deviceName.trim()
                if (deviceNameFormatted.startsWith(Constants.DEVICENAME_PREFIX)) {
                    deviceNameFormatted = deviceNameFormatted
                        .replaceFirst(Constants.DEVICENAME_PREFIX, "")
                        .trim()
                }
                pairSetting = pairSetting.copy(device_name = deviceNameFormatted)
                val device = FDeviceFlipperZeroBleModel(
                    name = deviceNameFormatted,
                    address = address.orEmpty(),
                    uniqueId = deviceId ?: UUID.randomUUID().toString(),
                    hardwareColor = FlipperZeroBle.HardwareColor.fromValue(pairSetting.hardware_color.value)
                )
                persistedStorage.addDevice(device)
                persistedStorage.setCurrentDevice(deviceId)
            }
            pairSetting
        }
    }
}
