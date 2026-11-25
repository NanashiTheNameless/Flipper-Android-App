package dev.namelessnanashi.flipperzero.bridge.dao.api.delegates

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.Flow

interface FavoriteApi {
    suspend fun updateFavorites(keys: List<FlipperKeyPath>): List<FlipperKeyPath>

    suspend fun isFavorite(keyPath: FlipperKeyPath): Boolean

    suspend fun setFavorite(keyPath: FlipperKeyPath, isFavorite: Boolean)

    fun getFavoritesFlow(): Flow<List<FlipperKey>>

    suspend fun getFavorites(): List<FlipperKey>

    /** All favorite paths, including remote definitions and other non-key files. */
    fun getFavoritePathsFlow(): Flow<List<FlipperKeyPath>> =
        getFavoritesFlow().map { keys -> keys.map { it.getKeyPath() } }

    suspend fun getFavoritePaths(): List<FlipperKeyPath> = getFavorites().map { it.getKeyPath() }
}
