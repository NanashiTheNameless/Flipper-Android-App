package dev.namelessnanashi.flipperzero.keyedit.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class KeyEditDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onSave: (FlipperKey?) -> Unit = { onBack.invoke() },
            flipperKeyPath: FlipperKeyPath,
            title: String?
        ): KeyEditDecomposeComponent

        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onSave: (FlipperKey?) -> Unit = { onBack.invoke() },
            notSavedFlipperKey: NotSavedFlipperKey,
            title: String?
        ): KeyEditDecomposeComponent
    }
}
