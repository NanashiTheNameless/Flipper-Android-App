package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.zacsweers.metro.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.NonCancellable
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.coroutines.withContext
import kotlin.io.path.Path
import kotlin.io.path.pathString

/**
 * Checks that an installed app's .fap still exists, by listing its folder on the Flipper once.
 *
 * Upstream held one lock for the whole listing, so the folders (one per app category) were
 * listed one after another. Each folder now has its own pending listing: different folders are
 * listed in parallel and checks for the same folder share one request.
 */
class FapExistChecker @Inject constructor(
    private val fFeatureProvider: FFeatureProvider
) : LogTagProvider {
    override val TAG = "FapExistChecker"

    private val folderListings = mutableMapOf<String, CompletableDeferred<Set<String>>>()
    private val mutex = Mutex()

    suspend fun checkExist(path: String): Boolean = getFolderListing(folderOf(path)).contains(path)

    /**
     * Lists the folders of all [paths] at once, so later [checkExist] calls find them ready
     */
    suspend fun prefetch(paths: Collection<String>) = coroutineScope {
        paths.map(::folderOf).distinct().forEach { folder ->
            launch { getFolderListing(folder) }
        }
    }

    suspend fun invalidate() = mutex.withLock {
        folderListings.clear()
    }

    private suspend fun getFolderListing(folder: String): Set<String> {
        while (true) {
            currentCoroutineContext().ensureActive()
            var isOwner = false
            val listing = mutex.withLock {
                folderListings.getOrPut(folder) {
                    isOwner = true
                    CompletableDeferred()
                }
            }
            if (!isOwner) {
                try {
                    return listing.await()
                } catch (_: CancellationException) {
                    // The coroutine that was listing this folder got cancelled: list it here
                    continue
                }
            }
            try {
                val paths = listFolder(folder)
                listing.complete(paths)
                return paths
            } catch (e: Throwable) {
                // Never leave waiters on a listing that will not complete: they retry instead
                withContext(NonCancellable) {
                    mutex.withLock {
                        if (folderListings[folder] === listing) folderListings.remove(folder)
                    }
                }
                listing.cancel()
                throw e
            }
        }
    }

    private suspend fun listFolder(folder: String): Set<String> {
        val fStorageFeatureApi = fFeatureProvider.getSync<FStorageFeatureApi>()
        if (fStorageFeatureApi == null) {
            error { "#listFolder($folder) could not get FStorageFeatureApi" }
        }
        return fStorageFeatureApi
            ?.listingApi()
            ?.ls(folder)
            ?.getOrNull()
            .orEmpty()
            .map { item -> Path(folder).resolve(item.fileName).toString() }
            .toSet()
    }

    private fun folderOf(path: String) = Path(path).parent?.pathString ?: "/"
}
