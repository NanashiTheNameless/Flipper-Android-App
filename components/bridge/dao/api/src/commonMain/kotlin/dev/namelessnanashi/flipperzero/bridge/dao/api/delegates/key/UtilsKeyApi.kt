package dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import kotlinx.coroutines.flow.Flow

interface UtilsKeyApi {
    suspend fun markAsSynchronized(keyPath: FlipperKeyPath)

    suspend fun updateNote(keyPath: FlipperKeyPath, note: String)

    fun search(text: String): Flow<List<FlipperKey>>

    /**
     * We try to find the nearest name that has no conflict.
     * Example:
     * Conflict with path "nfc/My_card.nfc"
     * Try "nfc/My_card_1.nfc"... Failed
     * Try "nfc/My_card_2.nfc"... Success!
     */
    suspend fun findAvailablePath(keyPath: FlipperKeyPath): FlipperKeyPath
}
