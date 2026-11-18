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
        return favoritesPaths.map {
            val relativePath = it.replace("/any/", "").replace("/ext/", "")
            return@map relativePath.substringBefore("/") to relativePath.substringAfter("/")
        }.filter { (keyFolder, keyName) ->
            keyFolder.trim().isNotEmpty() || keyName.trim().isNotEmpty()
        }.map { (keyFolder, keyName) ->
            return@map FlipperFilePath(
                folder = keyFolder,
                nameWithExtension = keyName
            )
        }
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
        val newFavoritesFile = resultFavoritesList.joinToString("\n") {
            it.getPathOnFlipper()
        } + "\n"
        val newFavoritesBytes = newFavoritesFile.toByteArray()
        flipperKeyStorage.saveFile(
            FAVORITES_PATH,
            FlipperKeyContent.RawData(newFavoritesBytes)
        )
        val writtenBytes = flipperKeyStorage.loadFile(FAVORITES_PATH).openStream().use { it.readBytes() }
        check(writtenBytes.contentEquals(newFavoritesBytes)) {
            "favorites.txt on the Flipper has ${writtenBytes.size} bytes after writing ${newFavoritesBytes.size}"
        }
        return resultFavoritesList
    }
}
