package dev.namelessnanashi.flipperzero.info.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.info.impl.compose.screens.ComposableFullDeviceInfoScreen
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.DeviceStatusViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.BasicInfoViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.FullInfoViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.ShareFullInfoFileViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

class FullInfoDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val shareFullInfoViewModelProvider: Provider<ShareFullInfoFileViewModel>,
    private val basicInfoViewModelProvider: Provider<BasicInfoViewModel>,
    private val fullInfoViewModelProvider: Provider<FullInfoViewModel>,
    private val deviceStatusViewModelProvider: Provider<DeviceStatusViewModel>
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        ComposableFullDeviceInfoScreen(
            onBack = onBack::invoke,
            shareViewModel = viewModelWithFactory(key = null) {
                shareFullInfoViewModelProvider.invoke()
            },
            basicInfoViewModel = viewModelWithFactory(key = null) {
                basicInfoViewModelProvider.invoke()
            },
            fullInfoViewModel = viewModelWithFactory(key = null) {
                fullInfoViewModelProvider.invoke()
            },
            deviceStatusViewModel = viewModelWithFactory(key = null) {
                deviceStatusViewModelProvider.invoke()
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter
        ): FullInfoDecomposeComponent
    }
}
