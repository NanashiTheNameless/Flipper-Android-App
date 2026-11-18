package dev.namelessnanashi.flipperzero.bridge.connection.config.api.serialization

import dev.namelessnanashi.flipperzero.core.preference.pb.FlipperZeroBle.HardwareColor
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.PrimitiveKind
import kotlinx.serialization.descriptors.PrimitiveSerialDescriptor
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal object HardwareColorSerializer : KSerializer<HardwareColor> {
    override val descriptor: SerialDescriptor = PrimitiveSerialDescriptor(
        serialName = "dev.namelessnanashi.flipperzero.SavedDevice.HardwareColor",
        kind = PrimitiveKind.INT
    )

    override fun deserialize(decoder: Decoder): HardwareColor {
        val intValue = decoder.decodeInt()
        return HardwareColor.fromValue(intValue)
    }

    override fun serialize(encoder: Encoder, value: HardwareColor) {
        encoder.encodeInt(value.value)
    }
}
