package dev.namelessnanashi.flipperzero.widget.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class WidgetDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            widgetId: Int,
            onBack: DecomposeOnBackParameter
        ): WidgetDecomposeComponent<*>
    }
}
