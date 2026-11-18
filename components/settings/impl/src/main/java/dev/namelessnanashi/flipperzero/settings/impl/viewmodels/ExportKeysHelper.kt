package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import android.content.Context
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroBleModel
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.createClearNewFileWithMkDirs
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.share.SharableFile
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeoutOrNull
import java.util.zip.ZipEntry
import java.util.zip.ZipOutputStream
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface ExportKeysHelper {
    suspend fun createBackupArchive(): SharableFile?
}

private const val KEYS_ARCHIVE_FALLBACK_NAME = "Flipper-Keys.zip"
private const val FLIPPER_NAME_TIMEOUT_MS = 3_000L
private val UNSAFE_FILE_NAME_CHARS = Regex("[^A-Za-z0-9._-]+")

/**
 * Fork: `Flipper-<flipper name>-Keys.zip`, so exports from several Flippers stay apart.
 * Characters that are not safe in a file name become `_`.
 */
internal fun keysArchiveName(flipperName: String?): String {
    val name = flipperName
        ?.trim()
        ?.removePrefix("Flipper ")
        ?.replace(UNSAFE_FILE_NAME_CHARS, "_")
        ?.trim('_')
    return if (name.isNullOrEmpty()) KEYS_ARCHIVE_FALLBACK_NAME else "Flipper-$name-Keys.zip"
}

@ContributesBinding(AppGraph::class, binding<ExportKeysHelper>())
class ExportKeysHelperImpl @Inject constructor(
    private val simpleKeyApi: SimpleKeyApi,
    private val context: Context,
    private val devicePersistedStorage: FDevicePersistedStorage,
    private val featureProvider: FFeatureProvider
) : ExportKeysHelper, LogTagProvider {
    override val TAG = "ExportKeysHelper"

    override suspend fun createBackupArchive() = withContext(FlipperDispatchers.workStealingDispatcher) {
        val archiveName = keysArchiveName(getFlipperName())
        val keysZip = SharableFile(context, archiveName).apply {
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

    /**
     * The paired BLE device stores its name, which works offline. Over USB the stored name is
     * just "USB", so the connected Flipper is asked (briefly: the export must not wait for one).
     */
    private suspend fun getFlipperName(): String? {
        val device = devicePersistedStorage.getCurrentDevice().first()
        if (device is FDeviceFlipperZeroBleModel) {
            return device.name
        }
        return withTimeoutOrNull(FLIPPER_NAME_TIMEOUT_MS) {
            featureProvider.getSync<FGetInfoFeatureApi>()
                ?.get(FGetInfoApiProperty.DeviceInfo.DEVICE_NAME)
                ?.getOrNull()
        }
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
