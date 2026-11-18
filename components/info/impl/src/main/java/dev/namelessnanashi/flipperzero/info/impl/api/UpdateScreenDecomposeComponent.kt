package dev.namelessnanashi.flipperzero.info.impl.api

import androidx.compose.foundation.rememberScrollState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.bottombar.handlers.ResetTabDecomposeHandler
import dev.namelessnanashi.flipperzero.core.preference.pb.HardwareColor
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.info.impl.compose.screens.ComposableDeviceInfoScreen
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceScreenNavigationConfig
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.AlarmViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.ConnectViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.DeviceStatusViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.FirmwareUpdateViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.FlipperColorViewModel
import dev.namelessnanashi.flipperzero.info.impl.viewmodel.deviceinfo.BasicInfoViewModel
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.updater.api.UpdaterCardApi
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import dev.zacsweers.metro.Provider

@Suppress("LongParameterList")
class UpdateScreenDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val deeplink: Deeplink.BottomBar.DeviceTab.WebUpdate?,
    @Assisted private val navigator: StackNavigator<DeviceScreenNavigationConfig>,
    private val updaterCardApi: UpdaterCardApi,
    private val deviceStatusViewModelProvider: Provider<DeviceStatusViewModel>,
    private val connectViewModelProvider: Provider<ConnectViewModel>,
    private val flipperColorProvider: Provider<FlipperColorViewModel>,
    private val firmwareUpdateViewModelProvider: Provider<FirmwareUpdateViewModel>,
    private val alarmViewModelProvider: Provider<AlarmViewModel>,
    private val basicInfoViewModelProvider: Provider<BasicInfoViewModel>,
) : ScreenDecomposeComponent(componentContext), ResetTabDecomposeHandler {
    private val requestScrollToTopFlow = MutableStateFlow(false)

    @Suppress("NonSkippableComposable", "LongMethod")
    @Composable
    override fun Render() {
        val rootNavigation = LocalRootNavigation.current
        val deviceStatusViewModel = viewModelWithFactory(key = null) {
            deviceStatusViewModelProvider.invoke()
        }
        val deviceStatus by deviceStatusViewModel.getState().collectAsState()
        val updateState by deviceStatusViewModel.getUpdateState().collectAsState()
        val connectViewModel = viewModelWithFactory(key = null) {
            connectViewModelProvider.invoke()
        }
        val flipperColorViewModel = viewModelWithFactory(key = null) {
            flipperColorProvider.invoke()
        }
        val flipperColor by flipperColorViewModel.getFlipperColor().collectAsState()
        val firmwareUpdateViewModel = viewModelWithFactory(key = null) {
            firmwareUpdateViewModelProvider.invoke()
        }
        val supportState by firmwareUpdateViewModel.getState().collectAsState()
        val alarmViewModel = viewModelWithFactory(key = null) {
            alarmViewModelProvider.invoke()
        }
        val basicInfoViewModel = viewModelWithFactory(key = null) {
            basicInfoViewModelProvider.invoke()
        }
        val basicInfo by basicInfoViewModel.getDeviceInfo().collectAsState()

        val scrollState = rememberScrollState()
        val requestScrollToTop by requestScrollToTopFlow.collectAsState()
        LaunchedEffect(requestScrollToTop) {
            if (requestScrollToTop) {
                scrollState.animateScrollTo(0)
                requestScrollToTopFlow.emit(false)
            }
        }

        ComposableDeviceInfoScreen(
            updaterCardApi = updaterCardApi,
            onOpenFullDeviceInfo = {
                navigator.pushToFront(DeviceScreenNavigationConfig.FullInfo)
            },
            onOpenOptions = {
                navigator.pushToFront(DeviceScreenNavigationConfig.Options)
            },
            onStartUpdateRequest = {
                rootNavigation.push(RootScreenConfig.UpdateScreen(it))
            },
            deeplink = deeplink,
            deviceStatus = deviceStatus,
            connectViewModel = connectViewModel,
            hardwareColor = HardwareColor.fromValue(flipperColor.value), // TODO change after full migration
            supportedState = supportState,
            updateState = updateState,
            hasAlarm = alarmViewModel.hasAlarm.collectAsState().value,
            alarmOnFlipper = alarmViewModel::alarmOnFlipper,
            deviceInfo = basicInfo,
            scrollState = scrollState,
            componentContext = this,
            onOpenRemoteControl = {
                rootNavigation.push(RootScreenConfig.ScreenStreaming)
            }
        )
    }

    override fun onResetTab() {
        requestScrollToTopFlow.update { true }
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            deeplink: Deeplink.BottomBar.DeviceTab.WebUpdate?,
            navigator: StackNavigator<DeviceScreenNavigationConfig>
        ): UpdateScreenDecomposeComponent
    }
}
