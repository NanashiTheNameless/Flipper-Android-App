package dev.namelessnanashi.flipperzero.updater.impl.tasks

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcInvalidParametersException
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.BootApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.md5
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.core.progress.ProgressWrapperTracker
import com.flipperdevices.protobuf.system.RebootRequest
import com.flipperdevices.protobuf.system.UpdateResponse
import dev.namelessnanashi.flipperzero.updater.impl.model.IntFlashFullException
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import okio.Path
import okio.Path.Companion.toOkioPath
import okio.Path.Companion.toPath
import java.io.File
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface UploadToFlipperHelper {
    @Suppress("LongParameterList")
    suspend fun uploadToFlipper(
        flipperPath: String,
        updaterFolder: File,
        bootApi: BootApi,
        fListingStorageApi: FListingStorageApi,
        fFileUploadApi: FFileUploadApi,
        stateListener: suspend (UpdatingState) -> Unit
    )
}

@ContributesBinding(AppGraph::class, binding<UploadToFlipperHelper>())
class UploadToFlipperHelperImpl @Inject constructor() : UploadToFlipperHelper, LogTagProvider {
    override val TAG = "UploadToFlipperHelper"

    override suspend fun uploadToFlipper(
        flipperPath: String,
        updaterFolder: File,
        bootApi: BootApi,
        fListingStorageApi: FListingStorageApi,
        fFileUploadApi: FFileUploadApi,
        stateListener: suspend (UpdatingState) -> Unit
    ) {
        upload(
            folder = updaterFolder,
            pathOnFlipper = flipperPath,
            fListingStorageApi = fListingStorageApi,
            fFileUploadApi = fFileUploadApi,
            progressListener = { percent ->
                withContext(FlipperDispatchers.workStealingDispatcher) {
                    stateListener(
                        UpdatingState.UploadOnFlipper(
                            percent = percent
                        )
                    )
                }
            },
        )
        try {
            bootApi.systemUpdate("$flipperPath/update.fuf")
        } catch (e: FRpcInvalidParametersException) {
            val code = e.response.system_update_response?.code
            if (code == UpdateResponse.UpdateResultCode.IntFull) {
                throw IntFlashFullException()
            }
        } catch (e: FRpcException) {
            error("Failed send update request with status ${e.response.command_status}")
        } catch (e: Exception) {
            error("Failed send update request with status unknown exception: ${e.message}")
        }

        bootApi.reboot(RebootRequest.RebootMode.UPDATE)
            .onFailure { error(it) { "#uploadToFlipper could not reboot device" } }
    }

    private suspend fun upload(
        folder: File,
        pathOnFlipper: String,
        progressListener: ProgressListener,
        fListingStorageApi: FListingStorageApi,
        fFileUploadApi: FFileUploadApi,
    ) {
        val fileList = folder.walk().filterNot { it.isDirectory }.toList().filterNot { file ->
            val isAlreadyUploaded = fileAlreadyUploaded(
                file = file,
                pathOnFlipper = pathOnFlipper.toPath().resolve(file.name),
                fListingStorageApi = fListingStorageApi
            )
            if (isAlreadyUploaded) {
                info { "Skip $file because file already uploaded" }
                return@filterNot true
            } else {
                return@filterNot false
            }
        }
        var sizeUploaded: Long = 0
        var totalSize: Long = 0
        fileList.forEach {
            totalSize += it.length()
        }

        info { "Start upload $fileList" }

        fileList.forEach { singleFile ->
            val flipperFilePath = File(pathOnFlipper, singleFile.name).path
            fFileUploadApi.upload(
                pathOnFlipper = flipperFilePath,
                fileOnAndroid = singleFile.toOkioPath(),
                progressListener = ProgressWrapperTracker(
                    progressListener = progressListener,
                    min = sizeUploaded.toFloat() / totalSize.toFloat(),
                    max = (sizeUploaded + singleFile.length()).toFloat() / totalSize.toFloat()
                )
            )
            sizeUploaded += singleFile.length()
        }
    }

    private suspend fun fileAlreadyUploaded(
        file: File,
        pathOnFlipper: Path,
        fListingStorageApi: FListingStorageApi
    ): Boolean {
        val fileMd5 = file.inputStream()
            .use { fis -> fis.md5() }
        val md5Response = fListingStorageApi.lsWithMd5(pathOnFlipper.toString())
            .onFailure { error(it) { "#fileAlreadyUploaded Could not get lsWithMd5" } }
            .getOrNull()
            ?.firstOrNull()
        return md5Response?.md5 == fileMd5
    }
}
