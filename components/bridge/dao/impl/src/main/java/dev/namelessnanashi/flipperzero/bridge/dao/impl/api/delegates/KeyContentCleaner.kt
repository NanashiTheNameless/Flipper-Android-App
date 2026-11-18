package dev.namelessnanashi.flipperzero.bridge.dao.impl.api.delegates

/**
 * Help delete unused files in internal storage
 */
interface KeyContentCleaner {
    suspend fun deleteUnusedFiles()
}
