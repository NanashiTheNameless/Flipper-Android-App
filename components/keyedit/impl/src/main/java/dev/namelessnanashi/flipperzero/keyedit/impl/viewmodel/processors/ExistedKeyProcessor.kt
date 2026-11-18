package dev.namelessnanashi.flipperzero.keyedit.impl.viewmodel.processors

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UpdateKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.namelessnanashi.flipperzero.keyedit.impl.model.KeyEditState
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.zacsweers.metro.Inject

class ExistedKeyProcessor @Inject constructor(
    private val simpleKeyApi: SimpleKeyApi,
    private val updateKeyApi: UpdateKeyApi,
    private val synchronizationApi: SynchronizationApi,
    private val parser: KeyParser
) : EditableKeyProcessor<EditableKey.Existed> {
    override suspend fun loadKey(
        editableKey: EditableKey.Existed,
        onStateUpdate: suspend (KeyEditState) -> Unit
    ) {
        val flipperKey = simpleKeyApi.getKey(editableKey.flipperKeyPath)
        if (flipperKey == null) {
            onStateUpdate(KeyEditState.Failed)
            return
        }
        val parsedKeyLoaded = parser.parseKey(flipperKey)
        onStateUpdate(
            KeyEditState.Editing(
                flipperKey.path.nameWithoutExtension,
                flipperKey.notes,
                parsedKeyLoaded,
                flipperKey.path.nameWithoutExtension.isNotBlank()
            )
        )
    }

    override suspend fun onSave(
        editableKey: EditableKey.Existed,
        editState: KeyEditState.Editing,
        onEndAction: (FlipperKey?) -> Unit
    ) {
        val oldKey = simpleKeyApi.getKey(editableKey.flipperKeyPath) ?: kotlin.run {
            onEndAction.invoke(null)
            return
        }
        val extension = editableKey.flipperKeyPath.path
            .nameWithExtension
            .substringAfterLast('.')
        val newFlipperKey = oldKey.copy(
            mainFile = oldKey.mainFile.copy(
                path = FlipperFilePath(
                    editableKey.flipperKeyPath.path.folder,
                    "${editState.name}.$extension"
                )
            ),
            notes = editState.notes
        )

        try {
            updateKeyApi.updateKey(oldKey, newFlipperKey)
            synchronizationApi.startSynchronization(force = true)
        } finally {
            onEndAction(newFlipperKey)
        }
    }
}
