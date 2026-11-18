package dev.namelessnanashi.flipperzero.faphub.screenshotspreview.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.screenshotspreview.api.model.ScreenshotsPreviewParam
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class ScreenshotsPreviewDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            param: ScreenshotsPreviewParam,
            onBack: DecomposeOnBackParameter,
        ): ScreenshotsPreviewDecomposeComponent
    }
}
