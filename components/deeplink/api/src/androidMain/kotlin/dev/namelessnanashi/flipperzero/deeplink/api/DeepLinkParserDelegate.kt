package dev.namelessnanashi.flipperzero.deeplink.api

import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink

interface DeepLinkParserDelegate {
    fun getPriority(context: Context, intent: Intent): DeepLinkParserDelegatePriority?
    suspend fun fromIntent(context: Context, intent: Intent): Deeplink?
}
