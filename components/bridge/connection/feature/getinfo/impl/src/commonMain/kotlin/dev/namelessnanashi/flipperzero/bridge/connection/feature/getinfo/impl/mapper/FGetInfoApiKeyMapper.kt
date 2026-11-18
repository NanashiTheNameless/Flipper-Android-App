package dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.impl.mapper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiGroup
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty

class FGetInfoApiKeyMapper {
    private val deviceInfoKeyMapper = DeviceInfoKeyMapper()

    fun map(group: FGetInfoApiGroup, path: String): FGetInfoApiProperty {
        return when (group) {
            FGetInfoApiGroup.DEVICE_INFO -> deviceInfoKeyMapper.map(path)
            FGetInfoApiGroup.POWER_INFO,
            FGetInfoApiGroup.POWER_DEBUG -> FGetInfoApiProperty.Unknown(group, path)
        }
    }
}
