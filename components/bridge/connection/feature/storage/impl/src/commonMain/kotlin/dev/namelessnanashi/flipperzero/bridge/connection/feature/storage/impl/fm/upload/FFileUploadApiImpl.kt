package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.upload

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.utils.toRpc
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.progress.FixedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.copyWithProgress
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.DeleteRequest
import com.flipperdevices.protobuf.storage.MkdirRequest
import com.flipperdevices.protobuf.storage.RenameRequest
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.plus
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import okio.FileSystem
import okio.Path
import okio.Sink
import okio.buffer

private const val TEMP_SUFFIX = ".fztmp"

class FFileUploadApiImpl(
    private val rpcFeatureApi: FRpcFeatureApi,
    private val scope: CoroutineScope,
    private val fileSystem: FileSystem = FileSystem.SYSTEM
) : FFileUploadApi, LogTagProvider {
    override val TAG = "FFileUploadApi"

    override suspend fun upload(
        pathOnFlipper: String,
        fileOnAndroid: Path,
        priority: StorageRequestPriority,
        progressListener: FixedProgressListener?
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        runCatching {
            fileSystem.source(fileOnAndroid).buffer().use { source ->
                sink(pathOnFlipper, priority).use { sink ->
                    source.copyWithProgress(
                        sink,
                        progressListener,
                        sourceLength = {
                            fileSystem.metadata(fileOnAndroid).size
                        }
                    )
                }
            }
        }
    }

    override suspend fun mkdir(
        pathOnFlipper: String
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        rpcFeatureApi.requestOnce(
            Main(
                storage_mkdir_request = MkdirRequest(pathOnFlipper)
            ).wrapToRequest()
        ).map { }
    }

    /**
     * Writes to a temp file next to [pathOnFlipper] and renames it over the target only after
     * the Flipper confirmed the whole write (the firmware's rename replaces an existing file).
     * An interrupted or cancelled write therefore never truncates the real file.
     */
    override suspend fun sink(
        pathOnFlipper: String,
        priority: StorageRequestPriority
    ): Sink {
        val tempPath = pathOnFlipper + TEMP_SUFFIX
        return FFlipperSink(
            requestLooper = WriteRequestLooper(rpcFeatureApi, tempPath, priority.toRpc(), scope),
            onCommit = {
                runBlocking {
                    rpcFeatureApi.requestOnce(
                        Main(
                            storage_rename_request = RenameRequest(
                                old_path = tempPath,
                                new_path = pathOnFlipper
                            )
                        ).wrapToRequest(priority.toRpc())
                    ).getOrThrow()
                }
            },
            onFail = {
                runBlocking {
                    rpcFeatureApi.requestOnce(
                        Main(
                            storage_delete_request = DeleteRequest(path = tempPath)
                        ).wrapToRequest(priority.toRpc())
                    ).onFailure { error(it) { "Could not delete temp file $tempPath" } }
                }
            }
        )
    }

    override suspend fun move(
        oldPath: Path,
        newPath: Path,
    ): Result<Unit> = rpcFeatureApi.requestOnce(
        command = Main(
            storage_rename_request = RenameRequest(
                old_path = oldPath.toString(),
                new_path = newPath.toString()
            )
        ).wrapToRequest()
    ).map { }
}
