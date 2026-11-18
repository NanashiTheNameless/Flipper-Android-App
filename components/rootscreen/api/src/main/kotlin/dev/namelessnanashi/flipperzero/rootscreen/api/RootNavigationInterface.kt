package dev.namelessnanashi.flipperzero.rootscreen.api

import androidx.compose.runtime.staticCompositionLocalOf
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig

val LocalRootNavigation = staticCompositionLocalOf<RootNavigationInterface> {
    error("CompositionLocal LocalRootComponent not present")
}

interface RootNavigationInterface {
    fun push(config: RootScreenConfig)
}
