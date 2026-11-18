package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.core.progress.ProgressWrapperTracker
import dev.namelessnanashi.flipperzero.core.progress.copyWithProgress
import dev.namelessnanashi.flipperzero.faphub.utils.FapHubConstants
import kotlinx.coroutines.runBlocking
import okio.buffer
import okio.source
import java.io.File
import dev.zacsweers.metro.Inject

private const val UPLOAD_ATTEMPTS = 2

class FapActionUpload @Inject constructor(
    private val fFeatureProvider: FFeatureProvider,
) : LogTagProvider {
    override val TAG = "FapActionUpload"

    suspend fun upload(
        fapFile: File,
        progressListener: ProgressListener
    ): String {
        info { "#upload Start upload ${fapFile.absolutePath}" }
        val fStorageFeatureApi = fFeatureProvider
            .getSync<FStorageFeatureApi>()
            ?: run {
                error { "#upload could not get FStorageFeatureApi" }
                error("Could not get FStorageFeatureApi")
            }
        fStorageFeatureApi.uploadApi()
            .mkdir(FapHubConstants.FLIPPER_TMP_FOLDER_PATH)
            .onFailure { error(it) { "#upload could not create dir ${FapHubConstants.FLIPPER_TMP_FOLDER_PATH}" } }
        val fapPath = File(
            FapHubConstants.FLIPPER_TMP_FOLDER_PATH,
            "tmp.fap"
        ).absolutePath
        val progressWrapper = ProgressWrapperTracker(progressListener)
        val localMd5 = fapFile.source().buffer().use { it.readByteString().md5().hex() }
        // An interrupted transfer can leave a partial tmp.fap; never install one that
        // does not match the downloaded bundle
        repeat(UPLOAD_ATTEMPTS) { attempt ->
            runCatching {
                fapFile.source().buffer().use { source ->
                    fStorageFeatureApi.uploadApi().sink(fapPath).use { sink ->
                        source.copyWithProgress(
                            sink = sink,
                            sourceLength = { fapFile.length() },
                            progressListener = { current, max ->
                                runBlocking { progressWrapper.onProgress(current, max) }
                            }
                        )
                    }
                }
            }.onFailure { error(it) { "Failed upload tmp manifest" } }.getOrThrow()

            val flipperMd5 = fStorageFeatureApi.md5Api().md5(fapPath)
                .onFailure { error(it) { "#upload could not get md5 of $fapPath" } }
                .getOrNull()
            if (flipperMd5.equals(localMd5, ignoreCase = true)) {
                info { "#upload verified $fapPath (md5 $localMd5)" }
                return fapPath
            }
            error { "#upload md5 mismatch on attempt ${attempt + 1}: flipper=$flipperMd5 local=$localMd5" }
        }
        error("Uploaded app does not match the downloaded file after $UPLOAD_ATTEMPTS attempts")
    }
}
