package dev.namelessnanashi.flipperzero.toolstab.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.navigate
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bottombar.handlers.ResetTabDecomposeHandler
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.nfc.mfkey32.api.MfKey32DecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.api.RemoteControlsScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.toolstab.api.ToolsDecomposeComponent
import dev.namelessnanashi.flipperzero.toolstab.impl.model.ToolsNavigationConfig
import dev.namelessnanashi.flipperzero.toolstab.impl.model.toConfigStack
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.namelessnanashi.flipperzero.ui.decompose.popToRoot
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, ToolsDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class ToolsDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted deeplink: Deeplink.BottomBar.ToolsTab?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onDeeplink: (Deeplink.BottomBar) -> Unit,
    private val hubMainFactory: ToolsMainScreenDecomposeComponentImpl.Factory,
    private val mfKey32Factory: MfKey32DecomposeComponent.Factory,
    private val remoteControlsComponentFactory: RemoteControlsScreenDecomposeComponent.Factory,
) : ToolsDecomposeComponent<ToolsNavigationConfig>(),
    ComponentContext by componentContext,
    ResetTabDecomposeHandler {
    override val stack: Value<ChildStack<ToolsNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = ToolsNavigationConfig.serializer(),
        initialStack = { deeplink.toConfigStack() },
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: ToolsNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        ToolsNavigationConfig.Main -> hubMainFactory(
            componentContext = componentContext,
            navigation = navigation
        )

        ToolsNavigationConfig.MfKey32 -> mfKey32Factory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        ToolsNavigationConfig.RemoteControls -> remoteControlsComponentFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) },
            onDeeplink = onDeeplink
        )
    }

    override fun handleDeeplink(deeplink: Deeplink.BottomBar.ToolsTab) {
        navigation.navigate { deeplink.toConfigStack() }
    }

    override fun onResetTab() {
        navigation.popToRoot()
    }
}
