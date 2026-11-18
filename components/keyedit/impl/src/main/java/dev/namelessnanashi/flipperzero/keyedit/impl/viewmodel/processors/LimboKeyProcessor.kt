package dev.namelessnanashi.flipperzero.keyedit.impl.viewmodel.processors

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UtilsKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.keyedit.impl.R
import dev.namelessnanashi.flipperzero.keyedit.impl.model.EditableKey
import dev.namelessnanashi.flipperzero.keyedit.impl.model.KeyEditState
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import kotlinx.collections.immutable.toImmutableList
import dev.zacsweers.metro.Inject

class LimboKeyProcessor @Inject constructor(
    private val parser: KeyParser,
    private val utilsKeyApi: UtilsKeyApi,
    private val simpleKeyApi: SimpleKeyApi,
    private val synchronizationApi: SynchronizationApi,
    private val inAppNotificationStorage: InAppNotificationStorage
) : EditableKeyProcessor<EditableKey.Limb> {
    override suspend fun loadKey(
        editableKey: EditableKey.Limb,
        onStateUpdate: suspend (KeyEditState) -> Unit
    ) {
        val newPath = utilsKeyApi.findAvailablePath(
            FlipperKeyPath(
                editableKey.notSavedFlipperKey.mainFile.path,
                deleted = false
            )
        ).path

        val parsedKeyLoaded = parser.parseKey(editableKey.notSavedFlipperKey.toFlipperKey(newPath))
        onStateUpdate(
            KeyEditState.Editing(
                newPath.nameWithoutExtension,
                editableKey.notSavedFlipperKey.notes,
                parsedKeyLoaded,
                newPath.nameWithoutExtension.isNotBlank()
            )
        )
    }

    override suspend fun onSave(
        editableKey: EditableKey.Limb,
        editState: KeyEditState.Editing,
        onEndAction: (FlipperKey?) -> Unit
    ) {
        val newPathUnfree = if (editState.name != null) {
            editableKey.notSavedFlipperKey.mainFile.path.copyWithChangedName(editState.name)
        } else {
            editableKey.notSavedFlipperKey.mainFile.path
        }
        val newPath = utilsKeyApi.findAvailablePath(
            FlipperKeyPath(newPathUnfree, deleted = false)
        ).path

        val newKey = editableKey.notSavedFlipperKey.toFlipperKey(newPath).copy(
            notes = editState.notes
        )

        simpleKeyApi.insertKey(newKey)
        synchronizationApi.startSynchronization(force = true)
        inAppNotificationStorage.addNotification(
            InAppNotification.Successful(
                title = newKey.path.nameWithExtension,
                descId = R.string.saved_key_desc
            )
        )
        onEndAction(newKey)
    }
}

private fun NotSavedFlipperKey.toFlipperKey(newPath: FlipperFilePath) = FlipperKey(
    mainFile = FlipperFile(newPath, mainFile.content),
    additionalFiles = additionalFiles.map {
        FlipperFile(
            it.path.copyWithChangedName(newPath.nameWithoutExtension),
            it.content
        )
    }.toImmutableList(),
    notes = notes,
    deleted = false,
    synchronized = false
)
