package dev.namelessnanashi.flipperzero.bridge.dao.impl.md5

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import okio.Path

interface MD5FileProvider {
    /**
     * From provided [contentMd5] and [keyContent] returns either
     * new or the file with same md5 and content if already exists
     */
    suspend fun getPathToFile(
        contentMd5: String,
        keyContent: FlipperKeyContent
    ): Path
}
