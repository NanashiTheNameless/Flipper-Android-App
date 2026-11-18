package dev.namelessnanashi.flipperzero.updater.impl.tasks.downloader

import dev.namelessnanashi.flipperzero.updater.model.UpdateContent
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import java.io.File

interface UpdateContentDownloader {
    fun isSupport(updateContent: UpdateContent): Boolean

    suspend fun downloadFirmwareLocal(
        updateContent: UpdateContent,
        updaterFolder: File,
        stateListener: suspend (UpdatingState) -> Unit
    )
}
