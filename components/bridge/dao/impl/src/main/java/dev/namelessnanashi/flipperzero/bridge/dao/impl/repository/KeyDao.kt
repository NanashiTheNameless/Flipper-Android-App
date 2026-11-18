package dev.namelessnanashi.flipperzero.bridge.dao.impl.repository

import androidx.room.Dao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.DeleteKeyDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.SimpleKeyDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.key.UtilsKeyDao

/**
 * By default all method exclude deleted field
 */
@Dao
interface KeyDao : DeleteKeyDao, SimpleKeyDao, UtilsKeyDao
