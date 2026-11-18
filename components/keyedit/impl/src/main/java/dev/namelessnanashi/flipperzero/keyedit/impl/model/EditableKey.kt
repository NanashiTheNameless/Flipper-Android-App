package dev.namelessnanashi.flipperzero.keyedit.impl.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import kotlinx.serialization.Serializable

@Serializable
sealed class EditableKey {
    @Serializable
    data class Existed(val flipperKeyPath: FlipperKeyPath) : EditableKey()

    @Serializable
    data class Limb(val notSavedFlipperKey: NotSavedFlipperKey) : EditableKey()
}
