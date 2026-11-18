package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.android.filename
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.withContext
import java.io.File
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkFileUriCopy @Inject constructor() : DeepLinkParserDelegate, LogTagProvider {
    override val TAG = "DeepLinkFileUriCopy"

    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        if (intent.data != null) {
            return DeepLinkParserDelegatePriority.LOW
        }
        return null
    }

    // Fallback if DeepLinkFileUriGrantPermission failed: copy from uri to tmp file
    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        val uri = intent.data ?: return null

        val contentResolver = context.contentResolver

        return Deeplink.RootLevel.SaveKey.ExternalContent(
            content = buildInternalFile(
                contentResolver,
                context.cacheDir,
                uri
            )
        )
    }

    private suspend fun buildInternalFile(
        contentResolver: ContentResolver,
        cacheDir: File,
        uri: Uri
    ): DeeplinkContent? = withContext(FlipperDispatchers.workStealingDispatcher) {
        val filename = uri.filename(contentResolver) ?: System.currentTimeMillis().toString()
        val temporaryFile = File(cacheDir, filename)
        if (temporaryFile.exists()) {
            temporaryFile.delete()
        }
        val exception = runCatching {
            contentResolver.openInputStream(uri).use { inputStream ->
                temporaryFile.outputStream().use { outputStream ->
                    inputStream?.copyTo(outputStream)
                }
            }
        }.exceptionOrNull()

        if (exception != null) {
            error(exception) { "Error while copy uri $uri to internal file $temporaryFile" }
            return@withContext null
        }

        return@withContext DeeplinkContent.InternalStorageFile(temporaryFile.absolutePath)
    }
}
