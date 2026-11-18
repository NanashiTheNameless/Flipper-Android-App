package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.composable.ComposableFapScreen
import dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.model.FapScreenNavigationConfig
import dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.viewmodel.FapScreenViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.toFapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.uninstallbutton.api.FapUninstallApi
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@Suppress("LongParameterList")
class ScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val id: String,
    @Assisted private val navigation: StackNavigation<FapScreenNavigationConfig>,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fapScreenViewModelFactory: FapScreenViewModel.Factory,
    private val installationUIApi: FapInstallationUIApi,
    private val uninstallApi: FapUninstallApi,
    private val errorsRenderer: FapHubComposableErrorsRenderer
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val fapScreenViewModel = viewModelWithFactory(key = id) {
            fapScreenViewModelFactory(id)
        }
        val loadingState by fapScreenViewModel.getLoadingState().collectAsState()
        val controlState by fapScreenViewModel.getControlState().collectAsState()

        val deeplinkHandler = LocalDeeplinkHandler.current

        ComposableFapScreen(
            onBack = onBack::invoke,
            installationButton = { fapItem, modifier ->
                installationUIApi.ComposableButton(
                    config = fapItem?.toFapButtonConfig(),
                    modifier = modifier,
                    fapButtonSize = FapButtonSize.LARGE,
                    componentContext = this
                )
            },
            onOpenDeviceTab = {
                deeplinkHandler.handleDeeplink(Deeplink.BottomBar.OpenTab(DeeplinkBottomBarTab.DEVICE))
            },
            uninstallButton = { modifier, fapItem ->
                uninstallApi.ComposableFapUninstallButton(
                    modifier = modifier,
                    fapItem = fapItem
                )
            },
            errorsRenderer = errorsRenderer,
            loadingState = loadingState,
            controlState = controlState,
            onRefresh = fapScreenViewModel::onRefresh,
            onPressHide = {
                fapScreenViewModel.onPressHide(
                    it.isHidden,
                    onBack::invoke
                )
            },
            onOpenReport = {
                navigation.pushToFront(fapScreenViewModel.getReportAppNavigationConfig(it))
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            id: String,
            navigation: StackNavigation<FapScreenNavigationConfig>,
            onBack: DecomposeOnBackParameter
        ): ScreenDecomposeComponentImpl
    }
}
