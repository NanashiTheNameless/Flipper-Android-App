package dev.namelessnanashi.flipperzero.rootscreen.api

import androidx.compose.runtime.staticCompositionLocalOf
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink

val LocalDeeplinkHandler = staticCompositionLocalOf<RootDeeplinkHandler> {
    error("CompositionLocal LocalDeeplinkHandler not present")
}

interface RootDeeplinkHandler {
    fun handleDeeplink(deeplink: Deeplink)
}
