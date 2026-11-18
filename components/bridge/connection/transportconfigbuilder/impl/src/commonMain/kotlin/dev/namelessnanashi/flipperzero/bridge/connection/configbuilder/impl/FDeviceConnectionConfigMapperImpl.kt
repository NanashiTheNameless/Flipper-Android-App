package dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.impl

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceBaseModel
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroUsbModel
import dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.api.FDeviceConnectionConfigMapper
import dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.impl.builders.FlipperZeroBleBuilderConfig
import dev.namelessnanashi.flipperzero.bridge.connection.configbuilder.impl.builders.FlipperZeroUsbBuilderConfig
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FDeviceConnectionConfig
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FDeviceConnectionConfigMapper>())
class FDeviceConnectionConfigMapperImpl @Inject constructor(
    private val flipperZeroBleBuilderConfig: FlipperZeroBleBuilderConfig,
    private val flipperZeroUsbBuilderConfig: FlipperZeroUsbBuilderConfig
) : FDeviceConnectionConfigMapper {
    override fun getConnectionConfig(device: FDeviceBaseModel): FDeviceConnectionConfig<*> {
        return when (device) {
            is FDeviceFlipperZeroBleModel -> flipperZeroBleBuilderConfig.build(device.address)
            is FDeviceFlipperZeroUsbModel -> flipperZeroUsbBuilderConfig.build(device.portPath)
        }
    }
}
