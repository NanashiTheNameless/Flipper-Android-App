package dev.namelessnanashi.flipperzero.keyedit.impl.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyedit.api.KeyEditDecomposeComponent
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<KeyEditDecomposeComponent.Factory>())
class KeyEditDecomposeComponentFactory @Inject constructor(
    private val keyEditRealFactory: KeyEditDecomposeComponentImpl.Factory
) : KeyEditDecomposeComponent.Factory {
    override fun invoke(
        componentContext: ComponentContext,
        onBack: DecomposeOnBackParameter,
        onSave: (FlipperKey?) -> Unit,
        flipperKeyPath: FlipperKeyPath,
        title: String?
    ): KeyEditDecomposeComponent {
        val editableKey = EditableKey.Existed(flipperKeyPath)
        return keyEditRealFactory(componentContext, onBack, onSave, editableKey, title)
    }

    override fun invoke(
        componentContext: ComponentContext,
        onBack: DecomposeOnBackParameter,
        onSave: (FlipperKey?) -> Unit,
        notSavedFlipperKey: NotSavedFlipperKey,
        title: String?
    ): KeyEditDecomposeComponent {
        val editableKey = EditableKey.Limb(notSavedFlipperKey)
        return keyEditRealFactory(componentContext, onBack, onSave, editableKey, title)
    }
}
