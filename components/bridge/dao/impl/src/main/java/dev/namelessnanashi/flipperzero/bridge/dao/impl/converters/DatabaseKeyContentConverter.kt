package dev.namelessnanashi.flipperzero.bridge.dao.impl.converters

import android.os.Looper
import androidx.room.ProvidedTypeConverter
import androidx.room.TypeConverter
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.md5.MD5Converter
import dev.namelessnanashi.flipperzero.bridge.dao.impl.md5.MD5FileProvider
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.DatabaseKeyContent
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.verbose
import okio.buffer
import okio.source
import dev.zacsweers.metro.Inject

@ProvidedTypeConverter
class DatabaseKeyContentConverter @Inject constructor(
    private val md5Converter: MD5Converter,
    private val mD5FileProvider: MD5FileProvider,
    private val flipperStorageProvider: FlipperStorageProvider
) : LogTagProvider {
    override val TAG = "DatabaseKeyContentConverter"

    @TypeConverter
    fun pathToKeyContent(path: String?): DatabaseKeyContent? {
        val pathNotNull = path ?: return null
        return DatabaseKeyContent(FlipperKeyContent.InternalFile(pathNotNull))
    }

    @TypeConverter
    fun keyContentToPath(keyContent: DatabaseKeyContent?): String? {
        if (BuildKonfig.CRASH_APP_ON_FAILED_CHECKS && Looper.getMainLooper() == Looper.myLooper()) {
            error("This method can be executed only on background thread!")
        }

        val keyContentNotNull = keyContent?.flipperContent ?: return null

        return runBlockingWithLog("convert") {
            keyContentToPathInternal(keyContentNotNull)
        }
    }

    private suspend fun keyContentToPathInternal(keyContent: FlipperKeyContent): String {
        val contentMd5 = md5Converter.convert(keyContent.openStream())

        val pathToFile = mD5FileProvider.getPathToFile(contentMd5, keyContent)
        if (flipperStorageProvider.fileSystem.exists(pathToFile)) {
            return pathToFile.normalized().toString()
        }

        flipperStorageProvider.mkdirsParent(pathToFile)
        verbose { "Create new file with hash $contentMd5" }

        flipperStorageProvider.fileSystem.sink(pathToFile).buffer().use { fileSink ->
            keyContent.openStream().source().buffer().use { contentSource ->
                fileSink.writeAll(contentSource)
            }
        }

        return pathToFile.normalized().toString()
    }
}
