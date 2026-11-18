package dev.namelessnanashi.flipperzero.faphub.fapscreen.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class FapScreenDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            id: String,
            onBack: DecomposeOnBackParameter
        ): FapScreenDecomposeComponent<*>
    }
}
