package dev.namelessnanashi.flipperzero.firstpair.impl.api

import ComposableSearchingView
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.firstpair.impl.viewmodels.connecting.PairDeviceViewModel
import dev.namelessnanashi.flipperzero.firstpair.impl.viewmodels.guide.ConnectionGuideViewModel
import dev.namelessnanashi.flipperzero.firstpair.impl.viewmodels.searching.BLEDeviceViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

fun interface OnClickHelp {
    operator fun invoke()
}

class DeviceScreenDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onFinishConnect: () -> Unit,
    @Assisted private val onHelpClick: OnClickHelp,
    private val bleDeviceViewModelProvider: Provider<BLEDeviceViewModel>,
    private val pairDeviceViewModelProvider: Provider<PairDeviceViewModel>,
    private val connectionGuideViewModelProvider: Provider<ConnectionGuideViewModel>
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val pairViewModel: PairDeviceViewModel = viewModelWithFactory(key = null) {
            pairDeviceViewModelProvider.invoke()
        }
        val bleDeviceViewModel: BLEDeviceViewModel = viewModelWithFactory(key = null) {
            bleDeviceViewModelProvider.invoke()
        }
        val usbViewModel: ConnectionGuideViewModel = viewModelWithFactory(key = null) {
            connectionGuideViewModelProvider.invoke()
        }
        val usbNotFound by usbViewModel.isUsbNotFound().collectAsState()
        ComposableSearchingView(
            onUseUsb = { usbViewModel.useUsb(onFinishConnect) },
            usbNotFound = usbNotFound,
            onHelpClicking = onHelpClick::invoke,
            onFinishConnection = onFinishConnect,
            onBack = onBack::invoke,
            bleDeviceViewModel = bleDeviceViewModel,
            pairViewModel = pairViewModel,
            lifecycleOwner = this
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onFinishConnect: () -> Unit,
            onHelpClick: OnClickHelp
        ): DeviceScreenDecomposeComponent
    }
}
