package dev.namelessnanashi.flipperzero.deeplink.impl.parser.delegates

import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.impl.utils.Constants
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private val PATHS = listOf("o", "update")

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class DeepLinkUpdate @Inject constructor() : DeepLinkParserDelegate, LogTagProvider {
    override val TAG = "DeepLinkFap"

    override fun getPriority(
        context: Context,
        intent: Intent
    ): DeepLinkParserDelegatePriority? {
        val pathSegment = intent.data?.pathSegments

        return when {
            intent.data == null -> null
            !Constants.SUPPORTED_HOSTS.contains(intent.data?.host) -> null
            pathSegment == null -> null
            pathSegment == PATHS -> DeepLinkParserDelegatePriority.HIGH
            else -> null
        }
    }

    override suspend fun fromIntent(context: Context, intent: Intent) = Deeplink.BottomBar.DeviceTab.OpenUpdate
}
