package dev.namelessnanashi.flipperzero.info.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bottombar.handlers.ResetTabDecomposeHandler
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.info.api.screen.DeviceScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.info.impl.model.DeviceScreenNavigationConfig
import dev.namelessnanashi.flipperzero.settings.api.SettingsDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.findComponentByConfig
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.namelessnanashi.flipperzero.ui.decompose.popToRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, DeviceScreenDecomposeComponent.Factory::class)
class DeviceScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted deeplink: Deeplink.BottomBar.DeviceTab?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val settingsFactory: SettingsDecomposeComponent.Factory,
    private val updateFactory: UpdateScreenDecomposeComponent.Factory,
    private val fullInfoDecomposeComponentFactory: FullInfoDecomposeComponent.Factory
) : DeviceScreenDecomposeComponent<DeviceScreenNavigationConfig>(),
    ComponentContext by componentContext,
    ResetTabDecomposeHandler {

    override val stack: Value<ChildStack<DeviceScreenNavigationConfig, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = DeviceScreenNavigationConfig.serializer(),
            initialConfiguration = DeviceScreenNavigationConfig.Update(
                deeplink as? Deeplink.BottomBar.DeviceTab.WebUpdate
            ),
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: DeviceScreenNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is DeviceScreenNavigationConfig.Update -> updateFactory(
            componentContext,
            config.deeplink,
            navigation
        )

        DeviceScreenNavigationConfig.FullInfo -> fullInfoDecomposeComponentFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        DeviceScreenNavigationConfig.Options -> settingsFactory(
            componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }

    override fun onResetTab() {
        navigation.popToRoot()
        val instance = stack.findComponentByConfig(DeviceScreenNavigationConfig.Update::class)
        if (instance is ResetTabDecomposeHandler) {
            instance.onResetTab()
        }
    }

    override fun handleDeeplink(deeplink: Deeplink.BottomBar.DeviceTab) {
        when (deeplink) {
            Deeplink.BottomBar.DeviceTab.OpenUpdate -> navigation.replaceAll(
                DeviceScreenNavigationConfig.Update(null)
            )

            is Deeplink.BottomBar.DeviceTab.WebUpdate -> navigation.replaceAll(
                DeviceScreenNavigationConfig.Update(deeplink)
            )
        }
    }
}
