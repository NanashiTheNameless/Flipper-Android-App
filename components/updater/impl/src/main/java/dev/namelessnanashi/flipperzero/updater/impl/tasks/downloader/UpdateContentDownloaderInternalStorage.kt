package dev.namelessnanashi.flipperzero.updater.impl.tasks.downloader

import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParser
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.updater.api.DownloadAndUnpackDelegateApi
import dev.namelessnanashi.flipperzero.updater.impl.model.UpdateContentException
import dev.namelessnanashi.flipperzero.updater.model.InternalStorageFirmware
import dev.namelessnanashi.flipperzero.updater.model.UpdateContent
import dev.namelessnanashi.flipperzero.updater.model.UpdatingState
import dev.zacsweers.metro.ContributesIntoSet
import java.io.File
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val MANIFEST_NAME = "update.fuf"

@ContributesIntoSet(AppGraph::class, binding<UpdateContentDownloader>())
class UpdateContentDownloaderInternalStorage @Inject constructor(
    private val downloadAndUnpackDelegateApi: DownloadAndUnpackDelegateApi,
    private val deeplinkParser: DeepLinkParser,
    private val context: Context
) : UpdateContentDownloader {
    override fun isSupport(updateContent: UpdateContent): Boolean {
        return updateContent is InternalStorageFirmware
    }

    @Suppress("ThrowsCount")
    override suspend fun downloadFirmwareLocal(
        updateContent: UpdateContent,
        updaterFolder: File,
        stateListener: suspend (UpdatingState) -> Unit
    ) {
        require(updateContent is InternalStorageFirmware) { "Content not compare" }
        val deeplink = deeplinkParser.fromUri(context, updateContent.uri)
        val deeplinkContent = if (deeplink is Deeplink.RootLevel.SaveKey.ExternalContent) {
            deeplink.content ?: throw UpdateContentException()
        } else {
            throw UpdateContentException()
        }

        if (deeplinkContent is DeeplinkContent.InternalStorageFile) {
            runCatching {
                downloadAndUnpackDelegateApi.unpack(deeplinkContent.file, updaterFolder)
            }.onFailure { throw UpdateContentException() }
        } else {
            throw UpdateContentException()
        }

        if (isManifestNotExist(updaterFolder)) throw UpdateContentException()
    }

    private fun isManifestNotExist(updaterFolder: File): Boolean {
        val files = updaterFolder.listFiles()?.firstOrNull()?.listFiles()
        val manifest = files?.filter { it.name == MANIFEST_NAME }
        return manifest?.isEmpty() ?: true
    }
}
