package dev.namelessnanashi.flipperzero.keyemulate.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.model.EmulateConfig
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.keyemulate.composable.ComposableInfraredSendButton
import dev.namelessnanashi.flipperzero.keyemulate.composable.ComposableSimpleEmulateButton
import dev.namelessnanashi.flipperzero.keyemulate.composable.ComposableSubGhzSendButton
import dev.namelessnanashi.flipperzero.keyemulate.viewmodel.InfraredViewModel
import dev.namelessnanashi.flipperzero.keyemulate.viewmodel.SimpleEmulateViewModel
import dev.namelessnanashi.flipperzero.keyemulate.viewmodel.SubGhzViewModel
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesBinding(AppGraph::class)
class KeyEmulateApiImpl @Inject constructor(
    private val infraredViewModel: Provider<InfraredViewModel>,
    private val simpleEmulateViewModel: Provider<SimpleEmulateViewModel>,
    private val subGhzViewModel: Provider<SubGhzViewModel>
) : KeyEmulateApi {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun ComposableEmulateButton(
        modifier: Modifier,
        emulateConfig: EmulateConfig,
        isSynchronized: Boolean,
        componentContext: ComponentContext
    ) {
        when (emulateConfig.keyType) {
            FlipperKeyType.SUB_GHZ -> ComposableSubGhzSendButton(
                modifier = modifier,
                emulateConfig = emulateConfig,
                isSynchronized = isSynchronized,
                emulateViewModel = componentContext.viewModelWithFactory(key = null) {
                    subGhzViewModel.invoke()
                }
            )

            FlipperKeyType.I_BUTTON,
            FlipperKeyType.RFID,
            FlipperKeyType.NFC -> ComposableSimpleEmulateButton(
                modifier = modifier,
                emulateConfig = emulateConfig,
                isSynchronized = isSynchronized,
                emulateViewModel = componentContext.viewModelWithFactory(key = null) {
                    simpleEmulateViewModel.invoke()
                }
            )

            FlipperKeyType.INFRARED -> ComposableInfraredSendButton(
                modifier = modifier,
                emulateConfig = emulateConfig,
                isSynchronized = isSynchronized,
                emulateViewModel = componentContext.viewModelWithFactory(key = null) {
                    infraredViewModel.invoke()
                }
            )
        }
    }
}
