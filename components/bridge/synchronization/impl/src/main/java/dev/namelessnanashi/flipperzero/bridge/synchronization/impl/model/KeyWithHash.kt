package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import kotlinx.serialization.ExperimentalSerializationApi
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.JsonNames

@OptIn(ExperimentalSerializationApi::class)
@Serializable
data class KeyWithHash(
    @JsonNames("keyPath")
    @SerialName("key_path")
    val keyPath: FlipperFilePath,
    @SerialName("hash")
    val hash: String
)
