package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.ListingItemWithHash
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileType
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyWithHash
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.debug
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import kotlin.io.path.Path
import dev.zacsweers.metro.binding

const val SIZE_BYTES_LIMIT = 10 * 1024 * 1024 // 10MiB

interface FlipperHashRepository {
    data class HashesProgressDetail(
        val keyType: FlipperKeyType
    ) : DetailedProgressListener.Detail

    suspend fun getHashesForType(
        keyType: FlipperKeyType,
        tracker: DetailedProgressWrapperTracker
    ): List<KeyWithHash>
}

@ContributesBinding(TaskGraph::class, binding<FlipperHashRepository>())
class FlipperHashRepositoryImpl @Inject constructor(
    private val flipperStorageApi: FListingStorageApi
) : FlipperHashRepository, LogTagProvider {
    override val TAG = "HashRepository"

    override suspend fun getHashesForType(
        keyType: FlipperKeyType,
        tracker: DetailedProgressWrapperTracker
    ): List<KeyWithHash> {
        tracker.onProgress(
            current = 0f,
            detail = FlipperHashRepository.HashesProgressDetail(keyType)
        )

        val fileTypePath = Path("/ext/").resolve(keyType.flipperDir).toString()
        val files = flipperStorageApi.lsWithMd5(fileTypePath)
            .onFailure { error(it) { "#getHashesForType could not get directories" } }
            .getOrNull()
            .orEmpty()
        info { "Receive ${files.size} files" }
        tracker.onProgress(
            current = 1f,
            detail = FlipperHashRepository.HashesProgressDetail(keyType)
        )
        return files.filter { isValidFile(it, keyType) }.map {
            KeyWithHash(keyPath = FlipperFilePath(keyType.flipperDir, it.fileName), hash = it.md5)
        }
    }

    private fun isValidFile(nameWithHash: ListingItemWithHash, requestedType: FlipperKeyType): Boolean {
        if (nameWithHash.fileType != FileType.FILE) {
            debug {
                "File ${nameWithHash.fileName} is not file. This is folder. Ignore it"
            }
            return false
        }
        if (nameWithHash.size > SIZE_BYTES_LIMIT) {
            debug {
                "File ${nameWithHash.fileName} skip," +
                    " because current size limit is $SIZE_BYTES_LIMIT, " +
                    "but file size is ${nameWithHash.size}"
            }
            return false
        }
        if (nameWithHash.fileName.startsWith(".")) {
            debug {
                "File ${nameWithHash.fileName} skip, because it starts with dot"
            }
            return false
        }
        val extension = nameWithHash.fileName.substringAfterLast(".")

        if (FlipperFileType.getByExtension(extension) == FlipperFileType.SHADOW_NFC &&
            requestedType == FlipperKeyType.NFC
        ) {
            return true
        }
        if (FlipperFileType.getByExtension(extension) == FlipperFileType.UI_INFRARED &&
            requestedType == FlipperKeyType.INFRARED
        ) {
            return true
        }
        val fileTypeByExtension = FlipperKeyType.getByExtension(extension)
        if (fileTypeByExtension == null) {
            debug {
                "File ${nameWithHash.fileName} skip, because we don't support this file extension ($extension)"
            }
            return false
        }
        if (fileTypeByExtension != requestedType) {
            debug {
                "File ${nameWithHash.fileName} skip, because folder type ($requestedType) " +
                    "and extension type ($fileTypeByExtension) is not equals"
            }
            return false
        }
        return true
    }
}
