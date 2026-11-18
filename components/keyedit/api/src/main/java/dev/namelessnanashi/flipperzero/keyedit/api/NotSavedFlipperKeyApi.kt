package dev.namelessnanashi.flipperzero.keyedit.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile

interface NotSavedFlipperKeyApi {
    suspend fun toNotSavedFlipperFile(flipperFile: FlipperFile): NotSavedFlipperFile
}
