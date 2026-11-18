package dev.namelessnanashi.flipperzero.keyemulate.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.ktx.image.Picture
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.InternalComposableEmulateButtonWithText
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.button.ComposableEmulateButton
import dev.namelessnanashi.flipperzero.keyemulate.composable.common.button.ComposableEmulatePlaceholder
import dev.namelessnanashi.flipperzero.keyemulate.model.EmulateProgress
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<KeyEmulateUiApi>())
class KeyEmulateUiApiImpl @Inject constructor() : KeyEmulateUiApi {
    @Composable
    override fun ComposableEmulateButtonRaw(
        modifier: Modifier,
        buttonContentModifier: Modifier,
        emulateProgress: EmulateProgress?,
        textId: Int,
        picture: Picture?,
        color: Color,
        progressColor: Color
    ) {
        ComposableEmulateButton(
            textId,
            picture,
            color,
            modifier,
            buttonContentModifier,
            emulateProgress,
            progressColor
        )
    }

    @Composable
    override fun ComposableEmulateButtonWithText(
        modifier: Modifier,
        buttonModifier: Modifier,
        progress: EmulateProgress?,
        buttonTextId: Int,
        textId: Int?,
        iconId: Int?,
        picture: Picture?,
        color: Color,
        progressColor: Color
    ) {
        InternalComposableEmulateButtonWithText(
            modifier = modifier,
            buttonModifier = buttonModifier,
            progress = progress,
            buttonTextId = buttonTextId,
            textId = textId,
            iconId = iconId,
            picture = picture,
            color = color,
            progressColor = progressColor
        )
    }

    @Composable
    override fun ComposableEmulatePlaceholderButton(
        modifier: Modifier
    ) {
        ComposableEmulatePlaceholder(
            modifier = modifier
        )
    }
}
