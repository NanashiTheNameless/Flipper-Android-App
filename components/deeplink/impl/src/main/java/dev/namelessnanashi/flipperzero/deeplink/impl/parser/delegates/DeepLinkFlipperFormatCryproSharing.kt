package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.impl.utils.Constants
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkFlipperFormatCryproSharing @Inject constructor(
    private val keyParser: KeyParser
) :
    DeepLinkParserDelegate,
    LogTagProvider {
    override val TAG = "DeepLinkFlipperFormatSharing"

    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        if (intent.data == null) {
            return null
        }
        if (Constants.SUPPORTED_HOSTS.contains(intent.data?.host)) {
            return DeepLinkParserDelegatePriority.HIGH
        }
        return null
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        val uri = intent.data ?: return null
        val flipperKeyCrypto = keyParser.parseUriToCryptoKeyData(uri) ?: return null

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
