package dev.namelessnanashi.flipperzero.share.receive.helpers

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.share.api.CryptoStorageApi
import dev.namelessnanashi.flipperzero.share.receive.models.FlipperKeyParseException
import dev.zacsweers.metro.Inject

class FlipperKeyParserHelper @Inject constructor(
    private val cryptoStorageApi: CryptoStorageApi
) {
    suspend fun toFlipperKey(link: Deeplink.RootLevel.SaveKey?): Result<FlipperKey> {
        if (link == null) return Result.failure(FlipperKeyParseException())
        var content: DeeplinkContent? = null
        var path: FlipperFilePath? = null
        when (link) {
            is Deeplink.RootLevel.SaveKey.ExternalContent -> content = link.content

            is Deeplink.RootLevel.SaveKey.FlipperKey -> {
                path = link.path
                content = link.content
            }
        }

        return when (content) {
            is DeeplinkContent.FFFContent -> parseFFFContent(content, path)
            is DeeplinkContent.InternalStorageFile -> parseInternalFile(content)
            is DeeplinkContent.FFFCryptoContent -> parseCryptoContent(path, content)
            else -> Result.failure(FlipperKeyParseException())
        }
    }

    private fun parseFFFContent(
        deeplinkContent: DeeplinkContent.FFFContent,
        path: FlipperFilePath?
    ): Result<FlipperKey> {
        val localPath = path ?: return Result.failure(FlipperKeyParseException())
        val key = FlipperKey(
            mainFile = FlipperFile(
                localPath,
                deeplinkContent.flipperFileFormat
            ),
            synchronized = false,
            deleted = false
        )
        return Result.success(key)
    }

    private fun parseInternalFile(
        deeplinkContent: DeeplinkContent.InternalStorageFile
    ): Result<FlipperKey> {
        val fileKey = deeplinkContent.file
        val flipperKey = FlipperKey(
            mainFile = FlipperFile(
                FlipperFilePath(
                    folder = fileKey.extension,
                    nameWithExtension = fileKey.name
                ),
                FlipperKeyContent.InternalFile(fileKey.absolutePath)
            ),
            synchronized = false,
            deleted = false
        )
        return Result.success(flipperKey)
    }

    private suspend fun parseCryptoContent(
        path: FlipperFilePath?,
        deeplinkContent: DeeplinkContent.FFFCryptoContent
    ): Result<FlipperKey> {
        val localPath = path ?: return Result.failure(FlipperKeyParseException())
        val flipperKeyContent = cryptoStorageApi.download(
            id = deeplinkContent.key.fileId,
            key = deeplinkContent.key.cryptoKey,
            name = localPath.nameWithExtension
        )
        flipperKeyContent.onSuccess {
            val flipperKey = FlipperKey(
                mainFile = FlipperFile(
                    localPath,
                    it
                ),
                synchronized = false,
                deleted = false
            )
            return Result.success(flipperKey)
        }
        val exception = flipperKeyContent.exceptionOrNull() ?: FlipperKeyParseException()
        return Result.failure(exception)
    }
}
