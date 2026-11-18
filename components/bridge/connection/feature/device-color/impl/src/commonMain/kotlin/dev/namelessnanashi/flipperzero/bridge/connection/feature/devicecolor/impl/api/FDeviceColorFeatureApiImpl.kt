package dev.namelessnanashi.flipperzero.bridge.connection.feature.devicecolor.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.bridge.connection.feature.devicecolor.api.FDeviceColorFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle.HardwareColor
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow

class FDeviceColorFeatureApiImpl @AssistedInject constructor(
    @Assisted private val fGetInfoFeatureApi: FGetInfoFeatureApi,
    private val fDevicePersistedStorage: FDevicePersistedStorage
) : FDeviceColorFeatureApi,
    LogTagProvider {
    override val TAG = "FDeviceColorFeatureApi"

    override fun updateAndGetColorFlow(default: HardwareColor): Flow<HardwareColor> = flow {
        val currentColoredDevice = fDevicePersistedStorage
            .getCurrentDevice()
            .first() as? FDeviceFlipperZeroBleModel
        if (currentColoredDevice != null) {
            emit(currentColoredDevice.hardwareColor)
        }

        val intHardwareColor = fGetInfoFeatureApi.get(FGetInfoApiProperty.DeviceInfo.HARDWARE_COLOR)
            .onFailure {
                error(
                    it
                ) { "#updateAndGetColorFlow could not fetch ${FGetInfoApiProperty.DeviceInfo.HARDWARE_COLOR}" }
            }
            .map { it.toIntOrNull() }
            .getOrNull()

        val hardwareColor = when (intHardwareColor) {
            HardwareColor.WHITE.value -> HardwareColor.WHITE
            HardwareColor.BLACK.value -> HardwareColor.BLACK
            HardwareColor.TRANSPARENT.value -> HardwareColor.TRANSPARENT
            else -> default
        }

        fDevicePersistedStorage.updateCurrentDevice { fDeviceBaseModel ->
            (fDeviceBaseModel as? FDeviceFlipperZeroBleModel)
                ?.copy(hardwareColor = hardwareColor)
                ?: fDeviceBaseModel
        }
        emit(hardwareColor)
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            fGetInfoFeatureApi: FGetInfoFeatureApi,
        ): FDeviceColorFeatureApiImpl
    }
}
