package dev.namelessnanashi.flipperzero.debug.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class StressTestDecomposeComponent(componentContext: ComponentContext) : ScreenDecomposeComponent(
    componentContext
) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): StressTestDecomposeComponent
    }
}
