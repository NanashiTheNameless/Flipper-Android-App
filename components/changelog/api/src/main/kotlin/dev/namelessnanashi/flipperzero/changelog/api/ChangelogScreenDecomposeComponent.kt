package dev.namelessnanashi.flipperzero.changelog.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest

abstract class ChangelogScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            updateRequest: UpdateRequest,
            onBack: DecomposeOnBackParameter
        ): ChangelogScreenDecomposeComponent
    }
}
