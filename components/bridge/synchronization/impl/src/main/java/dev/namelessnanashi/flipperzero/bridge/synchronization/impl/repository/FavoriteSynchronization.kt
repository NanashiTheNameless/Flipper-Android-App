package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.DiffSource
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepository
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.utils.KeyDiffCombiner
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface FavoriteSynchronization {
    data object FavoritesProgressDetail : DetailedProgressListener.Detail

    suspend fun syncFavorites(progressTracker: DetailedProgressWrapperTracker)
}

@ContributesBinding(TaskGraph::class, binding<FavoriteSynchronization>())
class FavoriteSynchronizationImpl @Inject constructor(
    private val favoriteApi: FavoriteApi,
    private val manifestRepository: ManifestRepository,
    private val flipperStorage: FlipperKeyStorage,
    private val favoritesRepository: FlipperFavoritesRepository
) : FavoriteSynchronization, LogTagProvider {
    override val TAG = "FavoriteSynchronization"

    override suspend fun syncFavorites(progressTracker: DetailedProgressWrapperTracker) {
        val favoritesFromFlipper = favoritesRepository.getFavorites(flipperStorage)
        progressTracker.onProgress(
            current = 0.5f,
            detail = FavoriteSynchronization.FavoritesProgressDetail
        )
        val favoritesFromAndroid = favoriteApi.getFavorites().map { it.path }
        val diffWithManifestAndFlipper = manifestRepository
            .compareFlipperFavoritesWithManifest(favoritesFromFlipper)
        val diffWithManifestAndAndroid = manifestRepository
            .compareFavoritesWithManifest(favoritesFromAndroid)

        info {
            "Receive favorites data. " +
                "Flipper: ${favoritesFromFlipper.size} " +
                "(Diff - ${diffWithManifestAndFlipper.size}). " +
                "Android: ${favoritesFromAndroid.size} " +
                "(Diff - ${diffWithManifestAndAndroid.size})"
        }

        val combinedDiff = KeyDiffCombiner.combineKeyDiffs(
            diffWithManifestAndFlipper,
            diffWithManifestAndAndroid
        ).sortedBy { it.action }
        info { "Favorites diff is $combinedDiff" }

        if (combinedDiff.isEmpty()) {
            manifestRepository.updateManifest(
                favorites = favoritesFromAndroid,
                favoritesOnFlipper = favoritesFromFlipper
            )
            return
        }

        val diffForFlipper = combinedDiff
            .filter { it.source == DiffSource.ANDROID }
        val newFavoritesOnFlipper = favoritesRepository.applyDiff(
            flipperStorage,
            favoritesFromFlipper,
            diffForFlipper
        ) // Update on Flipper
        val favoritesOnAndroid = favoriteApi.updateFavorites(
            newFavoritesOnFlipper.map {
                FlipperKeyPath(path = it, deleted = false)
            }
        )

        manifestRepository.updateManifest(
            favorites = favoritesOnAndroid.map { it.path },
            favoritesOnFlipper = newFavoritesOnFlipper
        )
        progressTracker.onProgress(
            current = 1f,
            detail = FavoriteSynchronization.FavoritesProgressDetail
        )
    }
}
