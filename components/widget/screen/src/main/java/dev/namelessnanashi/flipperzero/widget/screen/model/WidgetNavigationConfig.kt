package dev.namelessnanashi.flipperzero.widget.screen.model

import dev.namelessnanashi.flipperzero.archive.api.SelectKeyPathListener
import kotlinx.serialization.Serializable

@Serializable
sealed class WidgetNavigationConfig {
    @Serializable
    data class WidgetOptions(val widgetId: Int) : WidgetNavigationConfig()

    @Serializable
    data class SearchScreen(val listener: SelectKeyPathListener) : WidgetNavigationConfig()
}
