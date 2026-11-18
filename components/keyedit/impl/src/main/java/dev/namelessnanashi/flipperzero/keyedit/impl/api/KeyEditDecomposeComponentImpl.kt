package dev.namelessnanashi.flipperzero.keyedit.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.keyedit.api.KeyEditDecomposeComponent
import dev.namelessnanashi.flipperzero.keyedit.impl.composable.ComposableEditScreen
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.namelessnanashi.flipperzero.keyedit.impl.viewmodel.KeyEditViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class KeyEditDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val editableKey: EditableKey,
    @Assisted private val title: String?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onSave: (FlipperKey?) -> Unit,
    private val keyEditViewModelFactory: KeyEditViewModel.Factory
) : KeyEditDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val viewModel: KeyEditViewModel = viewModelWithFactory(key = editableKey.toString()) {
            keyEditViewModelFactory.invoke(editableKey)
        }
        val state by viewModel.getEditState().collectAsState()
        ComposableEditScreen(
            onNameChange = viewModel::onNameChange,
            onNoteChange = viewModel::onNotesChange,
            title = title,
            state = state,
            onBack = onBack::invoke,
            onSave = {
                viewModel.onSave(onSave::invoke)
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onSave: (FlipperKey?) -> Unit,
            editableKey: EditableKey,
            title: String?
        ): KeyEditDecomposeComponentImpl
    }
}
