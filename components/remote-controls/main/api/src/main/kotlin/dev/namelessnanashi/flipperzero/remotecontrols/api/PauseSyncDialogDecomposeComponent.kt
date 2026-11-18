package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class PauseSyncDialogDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onProceed: () -> Unit,
            onBack: DecomposeOnBackParameter,
        ): PauseSyncDialogDecomposeComponent
    }
}
