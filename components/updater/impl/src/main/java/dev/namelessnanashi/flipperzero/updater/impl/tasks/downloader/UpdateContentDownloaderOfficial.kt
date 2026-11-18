package dev.namelessnanashi.flipperzero.updater.impl.tasks.downloader

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.updater.model.OfficialFirmware
import dev.namelessnanashi.flipperzero.updater.model.UpdateContent
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.zacsweers.metro.ContributesIntoSet
import java.io.File
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<UpdateContentDownloader>())
class UpdateContentDownloaderOfficial @Inject constructor(
    private val firmwareDownloaderHelper: FirmwareDownloaderHelper
) : UpdateContentDownloader {
    override fun isSupport(updateContent: UpdateContent): Boolean {
        return updateContent is OfficialFirmware
    }

    override suspend fun downloadFirmwareLocal(
        updateContent: UpdateContent,
        updaterFolder: File,
        stateListener: suspend (UpdatingState) -> Unit
    ) {
        require(updateContent is OfficialFirmware) { "Content not compare" }
        stateListener(
            UpdatingState.DownloadingFromNetwork(0f)
        )
        firmwareDownloaderHelper.downloadFirmware(
            updateFile = updateContent.distributionFile,
            updaterFolder = updaterFolder,
            stateListener = stateListener
        )
    }
}
