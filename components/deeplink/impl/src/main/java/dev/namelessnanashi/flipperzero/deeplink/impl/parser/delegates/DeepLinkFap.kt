package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private val SUPPORTED_HOSTS = listOf("lab.flipper.net")
private const val PATH = "apps"

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkFap @Inject constructor() : DeepLinkParserDelegate, LogTagProvider {
    override val TAG = "DeepLinkFap"

    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        if (intent.data == null) {
            return null
        }

        if (!SUPPORTED_HOSTS.contains(intent.data?.host)) {
            return null
        }

        val pathSegment = intent.data?.pathSegments
            ?: return null

        if (pathSegment.size == 2 && pathSegment.first() == PATH) {
            return DeepLinkParserDelegatePriority.HIGH
        }

        return null
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        if (!SUPPORTED_HOSTS.contains(intent.data?.host)) {
            return null
        }
        val pathSegment = intent.data?.pathSegments ?: return null
        val fapId = pathSegment[1] ?: return null
        return Deeplink.BottomBar.AppsTab.Fap(fapId)
    }
}
