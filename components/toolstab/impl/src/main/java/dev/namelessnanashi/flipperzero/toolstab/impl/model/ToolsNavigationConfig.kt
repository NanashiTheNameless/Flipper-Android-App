package dev.namelessnanashi.flipperzero.toolstab.impl.model

import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import kotlinx.serialization.Serializable

@Serializable
sealed class ToolsNavigationConfig {
    @Serializable
    data object Main : ToolsNavigationConfig()

    @Serializable
    data object MfKey32 : ToolsNavigationConfig()

    @Serializable
    data object RemoteControls : ToolsNavigationConfig()
}

fun Deeplink.BottomBar.ToolsTab?.toConfigStack(): List<ToolsNavigationConfig> {
    val stack = mutableListOf<ToolsNavigationConfig>(ToolsNavigationConfig.Main)
    when (this) {
        is Deeplink.BottomBar.ToolsTab.OpenMfKey -> stack.add(ToolsNavigationConfig.MfKey32)
        null -> {}
    }
    return stack
}
