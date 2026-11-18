package dev.namelessnanashi.flipperzero.nfceditor.impl.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import kotlinx.serialization.Serializable

@Serializable
sealed class NfcEditorNavigationConfig {
    @Serializable
    data class NfcEditor(val flipperKeyPath: FlipperKeyPath) : NfcEditorNavigationConfig()

    @Serializable
    data class Save(val notSavedFlipperKey: NotSavedFlipperKey, val title: String?) : NfcEditorNavigationConfig()
}
