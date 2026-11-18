package dev.namelessnanashi.flipperzero.ifrmvp.model.serialization

import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.Base64ImageButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.ButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.ChannelButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.IconButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.NavigationButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.OkNavigationButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.PowerButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.ShutterButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.TextButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.UnknownButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.VolumeButtonData
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.decodeFromJsonElement
import kotlinx.serialization.json.jsonPrimitive

internal class ButtonDataDecoder(private val json: Json) {
    fun decodeFromJsonObject(jsonObject: JsonObject): ButtonData {
        val type = jsonObject["type"]?.jsonPrimitive?.content
        val buttonType = ButtonData.ButtonType.entries.find { entry -> entry.name == type }
        return when (buttonType) {
            ButtonData.ButtonType.BASE64_IMAGE -> {
                json.decodeFromJsonElement<Base64ImageButtonData>(jsonObject)
            }

            ButtonData.ButtonType.TEXT -> {
                json.decodeFromJsonElement<TextButtonData>(jsonObject)
            }

            ButtonData.ButtonType.CHANNEL -> {
                json.decodeFromJsonElement<ChannelButtonData>(jsonObject)
            }

            ButtonData.ButtonType.OK_NAVIGATION -> {
                json.decodeFromJsonElement<OkNavigationButtonData>(jsonObject)
            }

            ButtonData.ButtonType.VOLUME -> {
                json.decodeFromJsonElement<VolumeButtonData>(jsonObject)
            }

            ButtonData.ButtonType.UNKNOWN, null -> UnknownButtonData
            ButtonData.ButtonType.ICON -> {
                json.decodeFromJsonElement<IconButtonData>(jsonObject)
            }

            ButtonData.ButtonType.SHUTTER -> {
                json.decodeFromJsonElement<ShutterButtonData>(jsonObject)
            }

            ButtonData.ButtonType.NAVIGATION -> {
                json.decodeFromJsonElement<NavigationButtonData>(jsonObject)
            }
            ButtonData.ButtonType.POWER -> {
                json.decodeFromJsonElement<PowerButtonData>(jsonObject)
            }
        }
    }
}
