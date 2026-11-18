package dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata

import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@SerialName("VOL")
@Serializable
data class VolumeButtonData(
    @SerialName("add_key_id")
    val addKeyIdentifier: IfrKeyIdentifier = IfrKeyIdentifier.Unknown,
    @SerialName("reduce_key_id")
    val reduceKeyIdentifier: IfrKeyIdentifier = IfrKeyIdentifier.Unknown,
) : ButtonData {
    override val type: ButtonData.ButtonType = ButtonData.ButtonType.VOLUME
}
