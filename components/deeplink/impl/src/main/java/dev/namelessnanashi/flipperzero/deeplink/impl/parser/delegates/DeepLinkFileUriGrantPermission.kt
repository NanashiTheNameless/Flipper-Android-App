package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.ContentResolver
import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.android.filename
import dev.namelessnanashi.flipperzero.core.ktx.android.length
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkFileUriGrantPermission @Inject constructor() : DeepLinkParserDelegate {
    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        if (intent.data != null) {
            return DeepLinkParserDelegatePriority.DEFAULT
        }
        return null
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        val uri = intent.data ?: return null

        val contentResolver = context.contentResolver

        // We need persistable permission for read file on next activities
        val permissionGranted = runCatching {
            contentResolver.takePersistableUriPermission(uri, Intent.FLAG_GRANT_READ_URI_PERMISSION)
        }.isSuccess

        if (!permissionGranted) {
            return null
        }

        return Deeplink.RootLevel.SaveKey.ExternalContent(content = buildExternalUri(contentResolver, uri))
    }

    private suspend fun buildExternalUri(
        contentResolver: ContentResolver,
        uri: Uri
    ): DeeplinkContent = withContext(FlipperDispatchers.workStealingDispatcher) {
        return@withContext DeeplinkContent.ExternalUri(
            filename = uri.filename(contentResolver),
            size = uri.length(contentResolver),
            uriString = uri.toString()
        )
    }
}
