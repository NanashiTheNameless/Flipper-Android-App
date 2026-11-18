package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import android.content.Context
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.createClearNewFileWithMkDirs
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.share.SharableFile
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface ExportKeysHelper {
    suspend fun createBackupArchive(): SharableFile?
}

private const val KEYS_ARCHIVE_NAME = "keys.zip"

@ContributesBinding(AppGraph::class, binding<ExportKeysHelper>())
class ExportKeysHelperImpl @Inject constructor(
    private val simpleKeyApi: SimpleKeyApi,
    private val context: Context
) : ExportKeysHelper, LogTagProvider {
    override val TAG = "ExportKeysHelper"

    override suspend fun createBackupArchive() = withContext(FlipperDispatchers.workStealingDispatcher) {
        val keysZip = SharableFile(context, KEYS_ARCHIVE_NAME).apply {
            createClearNewFileWithMkDirs()
        }
        val keys = simpleKeyApi.getAllKeys(includeDeleted = false)
        if (keys.isEmpty()) {
            return@withContext null
        }

        keysZip.outputStream().use { fos ->
            ZipOutputStream(fos).use { out ->
                keys.forEach { key ->
                    insert(key.mainFile, out)
                    key.additionalFiles.forEach {
                        insert(it, out)
                    }
                }
            }
        }
        return@withContext keysZip
    }

    private suspend fun insert(
        flipperFile: FlipperFile,
        outputStream: ZipOutputStream
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        val entry = ZipEntry(flipperFile.path.pathToKey)
        outputStream.putNextEntry(entry)
        flipperFile.content.openStream().use {
            it.copyTo(outputStream)
        }
        outputStream.closeEntry()
    }
}
