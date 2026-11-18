package dev.namelessnanashi.flipperzero.info.impl.model

import androidx.compose.runtime.Stable
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.FlipperStorageInformation
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion

@Stable
data class FlipperBasicInfo(
    val firmwareVersion: FlipperInformationStatus<FirmwareVersion?> =
        FlipperInformationStatus.NotStarted(),
    val storageInfo: FlipperStorageInformation = FlipperStorageInformation()
)
