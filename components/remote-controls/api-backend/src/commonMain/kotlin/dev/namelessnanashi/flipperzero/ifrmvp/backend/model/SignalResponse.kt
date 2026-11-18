package dev.namelessnanashi.flipperzero.ifrmvp.backend.model

import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.ButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.serialization.ButtonDataSerializer
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class SignalResponse(
    @SerialName("signal_model")
    val signalModel: SignalModel,
    @SerialName("message")
    val message: String,
    @SerialName("category_name")
    val categoryName: String,
    @SerialName("data")
    @Serializable(ButtonDataSerializer::class)
    val data: ButtonData,
)
