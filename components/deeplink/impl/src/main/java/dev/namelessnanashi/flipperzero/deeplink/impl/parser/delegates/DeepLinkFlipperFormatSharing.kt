package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.impl.utils.Constants
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.withContext
import java.net.URLDecoder
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val SCHEME_FLIPPERKEY = "flipperkey"

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkFlipperFormatSharing @Inject constructor(
    private val parser: KeyParser
) : DeepLinkParserDelegate, LogTagProvider {
    override val TAG = "DeepLinkFlipperFormatSharing"

    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        val uri = intent.data ?: return null
        if (uri.scheme == SCHEME_FLIPPERKEY) {
            return DeepLinkParserDelegatePriority.HIGH
        }

        if (Constants.SUPPORTED_HOSTS.contains(intent.data?.host)) {
            return DeepLinkParserDelegatePriority.HIGH
        }

        return null
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        var pureUri = intent.data ?: return null
        info { "Try parse uri $pureUri. ${pureUri.query}${pureUri.fragment}" }

        if (pureUri.scheme == SCHEME_FLIPPERKEY) {
            var query = "${pureUri.query}"
            if (pureUri.fragment.isNullOrBlank().not()) {
                query += "#${pureUri.fragment}"
            }
            val decodedQuery = withContext(FlipperDispatchers.workStealingDispatcher) {
                URLDecoder.decode(query, "UTF-8")
            }
            pureUri = Uri.parse(decodedQuery)
            info { "Found flipper scheme, new uri is $pureUri" }
        }

        return getUrlDeeplink(pureUri) ?: getCryptoFileDeeplink(pureUri)
    }

    private suspend fun getUrlDeeplink(uri: Uri): Deeplink? {
        val (path, content) = parser.parseUri(uri) ?: return null

        return Deeplink.RootLevel.SaveKey.FlipperKey(
            path,
            DeeplinkContent.FFFContent(path.nameWithExtension, content)
        )
    }

    private fun getCryptoFileDeeplink(uri: Uri): Deeplink? {
        val flipperKeyCrypto = parser.parseUriToCryptoKeyData(uri)
        if (flipperKeyCrypto == null) {
            info { "Failed parse $uri because flipperKeyCrypto is null" }
            return null
        }

        val path = flipperKeyCrypto.pathToKey
        val flipperFilePath = FlipperFilePath(
            folder = path.substringBeforeLast("/"),
            nameWithExtension = path.substringAfterLast("/")
        )
        return Deeplink.RootLevel.SaveKey.FlipperKey(
            path = flipperFilePath,
            content = DeeplinkContent.FFFCryptoContent(key = flipperKeyCrypto)
        )
    }
}
