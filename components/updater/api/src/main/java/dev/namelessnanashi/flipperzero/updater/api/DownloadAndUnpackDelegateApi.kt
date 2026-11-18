package dev.namelessnanashi.flipperzero.updater.api

import dev.namelessnanashi.flipperzero.updater.model.DistributionFile
import java.io.File

interface DownloadAndUnpackDelegateApi {
    suspend fun download(
        distributionFile: DistributionFile,
        target: File,
        onProgress: (suspend (Long, Long?) -> Unit)? = null
    )

    suspend fun unpack(source: File, target: File)
}
