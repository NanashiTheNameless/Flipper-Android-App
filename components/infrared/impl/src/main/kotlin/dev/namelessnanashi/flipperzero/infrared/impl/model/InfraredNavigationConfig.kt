package dev.namelessnanashi.flipperzero.infrared.impl.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import kotlinx.serialization.Serializable

@Serializable
sealed class InfraredNavigationConfig {
    @Serializable
    data class View(val keyPath: FlipperKeyPath) : InfraredNavigationConfig()

    @Serializable
    data class Edit(val keyPath: FlipperKeyPath) : InfraredNavigationConfig()

    @Serializable
    data class Rename(val keyPath: FlipperKeyPath) : InfraredNavigationConfig()

    @Serializable
    data class RemoteControl(val keyPath: FlipperKeyPath) : InfraredNavigationConfig()
}
