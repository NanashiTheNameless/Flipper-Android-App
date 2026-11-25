package dev.namelessnanashi.flipperzero.firstpair.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

/**
 * Fork: the unofficial-build notice shown on app start (same content as the first-launch screen)
 */
abstract class ForkNoticeDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onClose: () -> Unit
        ): ForkNoticeDecomposeComponent
    }
}
