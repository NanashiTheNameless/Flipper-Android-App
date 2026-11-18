package dev.namelessnanashi.flipperzero.deeplink.impl.parser

import android.content.Context
import android.content.Intent
import android.net.Uri
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.android.toFullString
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.log.warn
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParser
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<DeepLinkParser>())
class DeepLinkParserImpl @Inject constructor(
    private val delegates: Set<DeepLinkParserDelegate>
) : DeepLinkParser, LogTagProvider {
    override val TAG = "DeepLinkParser"

    override suspend fun fromUri(context: Context, uri: Uri): Deeplink? {
        info { "Try parse uri with scheme: ${uri.scheme}, uri: $uri" }

        return fromIntent(
            context,
            Intent().apply {
                data = uri
            }
        )
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        info { "Try parse intent ${intent.toFullString()}" }

        val sortedDelegate = delegates.map { it to it.getPriority(context, intent) }
            .filter { (_, state) -> state != null }.sortedByDescending { (_, state) -> state }
            .map { (delegate, _) -> delegate }
        for (delegate in sortedDelegate) {
            try {
                info { "Try ${delegate.javaClass}..." }
                val deeplink = delegate.fromIntent(context, intent)
                if (deeplink != null) {
                    info { "Parsed deeplink: $deeplink. " }
                    return deeplink
                }
            } catch (e: Throwable) {
                error(e) { "Exception while try open ${intent.toFullString()}" }
            }
        }
        warn { "Failed parse intent ${intent.toFullString()}" }
        return null
    }
}
