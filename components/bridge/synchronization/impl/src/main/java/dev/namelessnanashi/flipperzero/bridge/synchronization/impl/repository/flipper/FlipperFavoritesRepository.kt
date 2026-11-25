package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception.FRpcStorageNotExistException
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyAction
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyDiff
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import java.nio.charset.Charset
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private val FAVORITES_PATH = FlipperFilePath("/", "favorites.txt")

interface FlipperFavoritesRepository {
    suspend fun getFavorites(flipperKeyStorage: FlipperKeyStorage): List<FlipperFilePath>
    suspend fun reorderFavorites(
        flipperKeyStorage: FlipperKeyStorage,
        oldFavorites: List<FlipperFilePath>,
        orderedKeys: List<FlipperFilePath>
    ): List<FlipperFilePath>

    suspend fun applyDiff(
        flipperKeyStorage: FlipperKeyStorage,
        oldFavorites: List<FlipperFilePath>,
        favoritesDiff: List<KeyDiff>
    ): List<FlipperFilePath>
}

@ContributesBinding(TaskGraph::class, binding<FlipperFavoritesRepository>())
class FlipperFavoritesRepositoryImpl @Inject constructor() :
    FlipperFavoritesRepository,
    LogTagProvider {
    override val TAG = "FavoritesRepository"

    override suspend fun getFavorites(flipperKeyStorage: FlipperKeyStorage): List<FlipperFilePath> {
        val favoritesPaths = getFavoritesFromFlipper(flipperKeyStorage)
        return favoritesPaths.filter { it.isNotBlank() }.map(FlipperFilePath::fromFavoritePath)
    }

    private suspend fun getFavoritesFromFlipper(
        flipperKeyStorage: FlipperKeyStorage
    ): List<String> = withContext(FlipperDispatchers.workStealingDispatcher) {
        val favoritesFile = try {
            flipperKeyStorage.loadFile(FAVORITES_PATH).openStream().use { stream ->
                stream.readBytes().toString(Charset.defaultCharset())
            }
        } catch (_: FRpcStorageNotExistException) {
            return@withContext emptyList()
        }
        return@withContext favoritesFile
            .split("\n")
    }

    override suspend fun applyDiff(
        flipperKeyStorage: FlipperKeyStorage,
        oldFavorites: List<FlipperFilePath>,
        favoritesDiff: List<KeyDiff>
    ): List<FlipperFilePath> {
        if (favoritesDiff.isEmpty()) {
            // Nothing changes on the Flipper; upstream rewrote the file anyway on every sync with
            // Flipper-side changes, which is how it got emptied (2026-10-29)
            return oldFavorites
        }
        val resultFavoritesList = ArrayList(oldFavorites)
        for (diff in favoritesDiff) {
            when (diff.action) {
                KeyAction.ADD -> resultFavoritesList.add(diff.newHash.keyPath)
                KeyAction.MODIFIED -> resultFavoritesList.add(diff.newHash.keyPath)
                KeyAction.DELETED -> resultFavoritesList.remove(diff.newHash.keyPath)
            }
        }
        if (resultFavoritesList.isEmpty() &&
            oldFavorites.isNotEmpty() &&
            favoritesDiff.none { it.action == KeyAction.DELETED }
        ) {
            // Only an explicit removal may empty the list; anything else is a bug upstream of here
            error { "Refusing to replace ${oldFavorites.size} favorites with an empty list" }
            return oldFavorites
        }
        writeFavorites(flipperKeyStorage, resultFavoritesList)
        return resultFavoritesList
    }

    override suspend fun reorderFavorites(
        flipperKeyStorage: FlipperKeyStorage,
        oldFavorites: List<FlipperFilePath>,
        orderedKeys: List<FlipperFilePath>
    ): List<FlipperFilePath> {
        // Permute only known keys. Keep firmware-only favorites in their original slots.
        val existing = oldFavorites.toSet()
        val ordered = orderedKeys.distinct().filter { it in existing }
        val known = ordered.toSet()
        val iterator = ordered.iterator()
        val result = oldFavorites.map { if (it in known) iterator.next() else it }
        if (result != oldFavorites) writeFavorites(flipperKeyStorage, result)
        return result
    }

    private suspend fun writeFavorites(
        flipperKeyStorage: FlipperKeyStorage,
        favorites: List<FlipperFilePath>
    ) {
        val newFavoritesFile = favorites.joinToString("\n") { it.getPathOnFlipper() } + "\n"
        val newFavoritesBytes = newFavoritesFile.toByteArray()
        flipperKeyStorage.saveFile(
            FAVORITES_PATH,
            FlipperKeyContent.RawData(newFavoritesBytes)
        )
        val writtenBytes = flipperKeyStorage.loadFile(FAVORITES_PATH).openStream().use { it.readBytes() }
        check(writtenBytes.contentEquals(newFavoritesBytes)) {
            "favorites.txt on the Flipper has ${writtenBytes.size} bytes after writing ${newFavoritesBytes.size}"
        }
    }
}
