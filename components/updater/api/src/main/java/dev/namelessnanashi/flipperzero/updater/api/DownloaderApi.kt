package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.DistributionFile
import dev.namelessnanashi.flipperzero.updater.model.DownloadProgress
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.SubGhzProvisioningException
import dev.namelessnanashi.flipperzero.updater.model.SubGhzProvisioningModel
import dev.namelessnanashi.flipperzero.updater.model.VersionFiles
import kotlinx.coroutines.flow.Flow
import java.io.File
import java.util.EnumMap

interface DownloaderApi {
    suspend fun getLatestVersion(): EnumMap<FirmwareChannel, VersionFiles>

    @Throws(SubGhzProvisioningException::class)
    suspend fun getSubGhzProvisioning(): SubGhzProvisioningModel

    fun download(
        distributionFile: DistributionFile,
        target: File,
        decompress: Boolean = false
    ): Flow<DownloadProgress>
}
