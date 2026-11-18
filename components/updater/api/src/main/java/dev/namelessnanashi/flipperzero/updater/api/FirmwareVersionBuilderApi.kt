package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion

interface FirmwareVersionBuilderApi {
    fun getFirmwareChannel(branch: String): FirmwareChannel
    fun buildFirmwareVersionFromString(firmwareVersion: String): FirmwareVersion
}
