package dev.namelessnanashi.flipperzero.keyemulate.api

import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.namelessnanashi.flipperzero.core.ui.ktx.image.Picture
import dev.namelessnanashi.flipperzero.keyemulate.model.EmulateProgress

interface KeyEmulateUiApi {
    @Composable
    fun ComposableEmulateButtonRaw(
        modifier: Modifier,
        buttonContentModifier: Modifier,
        emulateProgress: EmulateProgress?,
        @StringRes textId: Int,
        picture: Picture?,
        color: Color,
        progressColor: Color
    )

    @Composable
    fun ComposableEmulateButtonWithText(
        modifier: Modifier,
        buttonModifier: Modifier,
        progress: EmulateProgress?,
        @StringRes buttonTextId: Int,
        @StringRes textId: Int?,
        @DrawableRes iconId: Int?,
        picture: Picture?,
        color: Color,
        progressColor: Color
    )

    @Composable
    fun ComposableEmulatePlaceholderButton(
        modifier: Modifier,
    )
}
