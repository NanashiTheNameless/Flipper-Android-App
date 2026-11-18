package dev.namelessnanashi.flipperzero.bottombar.impl.api

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.extensions.compose.subscribeAsState
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import com.arkivanov.essenty.backhandler.BackCallback
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.namelessnanashi.flipperzero.archive.api.ArchiveDecomposeComponent
import dev.namelessnanashi.flipperzero.bottombar.api.BottomBarDecomposeComponent
import dev.namelessnanashi.flipperzero.bottombar.handlers.ResetTabDecomposeHandler
import dev.namelessnanashi.flipperzero.bottombar.impl.composable.ComposableMainScreen
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabEnum
import dev.namelessnanashi.flipperzero.bottombar.impl.model.toBottomBarTabEnum
import dev.namelessnanashi.flipperzero.bottombar.impl.model.toConfig
import dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel.BottomBarViewModel
import dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel.InAppNotificationViewModel
import dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel.SelectedTabViewModel
import dev.namelessnanashi.flipperzero.connection.api.ConnectionApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactoryWithoutRemember
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.faphub.main.api.FapHubDecomposeComponent
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationRenderer
import dev.namelessnanashi.flipperzero.info.api.screen.DeviceScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.toolstab.api.ToolsDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.findChildByConfig
import dev.namelessnanashi.flipperzero.ui.decompose.findComponentByConfig
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.namelessnanashi.flipperzero.unhandledexception.api.UnhandledExceptionRenderApi
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.runBlocking
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@ContributesAssistedFactory(AppGraph::class, BottomBarDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class BottomBarDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted deeplink: Deeplink.BottomBar?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val settingsDataStore: DataStore<Settings>,
    private val archiveScreenFactory: ArchiveDecomposeComponent.Factory,
    private val deviceScreenFactory: DeviceScreenDecomposeComponent.Factory,
    private val toolsScreenFactory: ToolsDecomposeComponent.Factory,
    private val fapHubScreenFactory: FapHubDecomposeComponent.Factory,
    private val connectionApi: ConnectionApi,
    private val notificationRenderer: InAppNotificationRenderer,
    private val unhandledExceptionRendererApi: UnhandledExceptionRenderApi,
    private val bottomBarViewModelProvider: Provider<BottomBarViewModel>,
    private val selectedTabViewModelProvider: Provider<SelectedTabViewModel>,
    private val inAppNotificationViewModelFactory: InAppNotificationViewModel.Factory
) : BottomBarDecomposeComponent<BottomBarTabConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<BottomBarTabConfig, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = BottomBarTabConfig.serializer(),
            initialConfiguration = BottomBarTabConfig.getInitialConfig(
                getSavedTab = {
                    instanceKeeper.getOrCreate("bbdc_st_vm") {
                        selectedTabViewModelProvider.invoke()
                    }.getSelectedTab()
                },
                deeplink = deeplink
            ),
            childFactory = ::child,
        )

    private val backCallback = BackCallback {
        navigation.popOr(onBack::invoke)
    }

    init {
        backHandler.register(backCallback)
    }

    private val notificationViewModel = viewModelWithFactoryWithoutRemember(key = this) {
        inAppNotificationViewModelFactory(this)
    }

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() = Box {
        val childStack by stack.subscribeAsState()
        val connectionTabState = connectionApi.getConnectionTabState(
            componentContext = this@BottomBarDecomposeComponentImpl
        )

        val bottomBarViewModel: BottomBarViewModel = viewModelWithFactory(key = null) {
            bottomBarViewModelProvider.invoke()
        }
        val toolsHasNotification by bottomBarViewModel.hasNotificationHubState().collectAsState()
        val appsHasNotification by bottomBarViewModel.hasNotificationAppsState().collectAsState()

        ComposableMainScreen(
            notificationViewModel = notificationViewModel,
            notificationRenderer = notificationRenderer,
            childStack = childStack,
            connectionTabState = connectionTabState,
            onTabClick = ::goToTab,
            modifier = Modifier.fillMaxSize(),
            toolsHasNotification = toolsHasNotification,
            appsHasNotification = appsHasNotification
        )

        connectionApi.CheckAndShowUnsupportedDialog(
            componentContext = this@BottomBarDecomposeComponentImpl
        )
        unhandledExceptionRendererApi.ComposableUnhandledExceptionRender(Modifier)
    }

    private fun child(
        config: BottomBarTabConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is BottomBarTabConfig.Archive -> archiveScreenFactory(
            componentContext = componentContext,
            deeplink = config.deeplink,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is BottomBarTabConfig.Device -> deviceScreenFactory(
            componentContext = componentContext,
            deeplink = config.deeplink,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is BottomBarTabConfig.Tools -> toolsScreenFactory(
            componentContext = componentContext,
            deeplink = config.deeplink,
            onBack = { navigation.popOr(onBack::invoke) },
            onDeeplink = ::handleDeeplink
        )

        is BottomBarTabConfig.Apps -> fapHubScreenFactory(
            componentContext = componentContext,
            deeplink = config.deeplink,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }

    private fun goToTab(bottomBarTabEnum: BottomBarTabEnum, force: Boolean) {
        runBlocking {
            settingsDataStore.updateData {
                it.copy(
                    selected_tab = bottomBarTabEnum.protobufRepresentation
                )
            }
        }

        val existedPair = stack.value.items.find { it.configuration.enum == bottomBarTabEnum }
        if (existedPair == null) {
            navigation.bringToFront(bottomBarTabEnum.toConfig())
            return
        }
        navigation.bringToFront(existedPair.configuration)

        if (force) {
            val instance = existedPair.instance
            if (instance is ResetTabDecomposeHandler) {
                instance.onResetTab()
            }
        }
    }

    override fun handleDeeplink(deeplink: Deeplink.BottomBar) {
        when (deeplink) {
            is Deeplink.BottomBar.ArchiveTab -> {
                val child = stack.findChildByConfig(BottomBarTabConfig.Archive::class)
                val instance = child?.instance
                if (instance == null || instance !is ArchiveDecomposeComponent<*>) {
                    navigation.bringToFront(BottomBarTabConfig.Archive(deeplink))
                } else {
                    navigation.bringToFront(child.configuration)
                    instance.handleDeeplink(deeplink)
                }
            }

            is Deeplink.BottomBar.DeviceTab -> {
                val instance = stack.findComponentByConfig(BottomBarTabConfig.Device::class)
                if (instance == null || instance !is DeviceScreenDecomposeComponent<*>) {
                    navigation.bringToFront(BottomBarTabConfig.Device(deeplink))
                } else {
                    instance.handleDeeplink(deeplink)
                }
            }

            is Deeplink.BottomBar.ToolsTab -> {
                val instance = stack.findComponentByConfig(BottomBarTabConfig.Tools::class)
                if (instance == null || instance !is ToolsDecomposeComponent<*>) {
                    navigation.bringToFront(BottomBarTabConfig.Tools(deeplink))
                } else {
                    instance.handleDeeplink(deeplink)
                }
            }

            is Deeplink.BottomBar.AppsTab -> {
                val instance = stack.findComponentByConfig(BottomBarTabConfig.Apps::class)
                if (instance == null || instance !is FapHubDecomposeComponent<*>) {
                    navigation.bringToFront(BottomBarTabConfig.Apps(deeplink))
                } else {
                    instance.handleDeeplink(deeplink)
                }
            }

            is Deeplink.BottomBar.OpenTab -> goToTab(
                deeplink.bottomTab.toBottomBarTabEnum(),
                force = true
            )
        }
    }
}
