package dev.namelessnanashi.flipperzero.keyedit.impl.viewmodel.processors

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.namelessnanashi.flipperzero.keyedit.impl.model.KeyEditState

interface EditableKeyProcessor<T : EditableKey> {
    suspend fun loadKey(editableKey: T, onStateUpdate: suspend (KeyEditState) -> Unit)
    suspend fun onSave(editableKey: T, editState: KeyEditState.Editing, onEndAction: (FlipperKey?) -> Unit)
}
