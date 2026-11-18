package dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key

import androidx.room.Query
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.Key
import kotlinx.coroutines.flow.Flow

interface DeleteKeyDao {
    @Query("UPDATE keys SET deleted = 1 WHERE path = :path AND deleted = 0")
    suspend fun markDeleted(path: String)

    @Query("UPDATE keys SET deleted = 0 WHERE path = :path AND deleted = 1")
    suspend fun restore(path: String)

    @Query("DELETE FROM keys WHERE path = :path AND deleted = 1")
    suspend fun deleteMarkedDeleted(path: String)

    @Query("SELECT * FROM keys")
    suspend fun getAllWithDeleted(): List<Key>

    @Query("SELECT * FROM keys WHERE deleted = 1")
    fun subscribeOnDeletedKeys(): Flow<List<Key>>
}
