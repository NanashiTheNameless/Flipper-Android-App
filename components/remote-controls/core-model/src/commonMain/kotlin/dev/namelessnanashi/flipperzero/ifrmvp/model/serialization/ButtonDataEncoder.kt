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
import kotlinx.serialization.json.encodeToJsonElement
import kotlinx.serialization.json.jsonObject

internal class ButtonDataEncoder(private val json: Json) {
    fun encodeToJsonObject(buttonData: ButtonData): JsonObject {
        return when (buttonData.type) {
            ButtonData.ButtonType.UNKNOWN -> {
                buttonData as UnknownButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.BASE64_IMAGE -> {
                buttonData as Base64ImageButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.TEXT -> {
                buttonData as TextButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.CHANNEL -> {
                buttonData as ChannelButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.OK_NAVIGATION -> {
                buttonData as OkNavigationButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.VOLUME -> {
                buttonData as VolumeButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.ICON -> {
                buttonData as IconButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.SHUTTER -> {
                buttonData as ShutterButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.NAVIGATION -> {
                buttonData as NavigationButtonData
                json.encodeToJsonElement(buttonData)
            }

            ButtonData.ButtonType.POWER -> {
                buttonData as PowerButtonData
                json.encodeToJsonElement(buttonData)
            }
        }.jsonObject
    }
}
