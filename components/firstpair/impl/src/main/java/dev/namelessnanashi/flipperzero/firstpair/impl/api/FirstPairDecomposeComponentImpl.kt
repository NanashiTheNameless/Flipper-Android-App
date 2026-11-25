package dev.namelessnanashi.flipperzero.firstpair.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.firstpair.api.ConnectionGuideDecomposeComponent
import dev.namelessnanashi.flipperzero.firstpair.api.FirstPairDecomposeComponent
import dev.namelessnanashi.flipperzero.firstpair.impl.model.FirstPairNavigationConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FirstPairDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class FirstPairDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val invalidate: () -> Unit,
    private val tosScreenFactory: TOSScreenDecomposeComponent.Factory,
    private val connectionGuideFactory: ConnectionGuideDecomposeComponent.Factory,
    private val deviceScreenFactory: DeviceScreenDecomposeComponent.Factory
) : FirstPairDecomposeComponent<FirstPairNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<FirstPairNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = FirstPairNavigationConfig.serializer(),
        initialConfiguration = if (ForkNoticeSession.markShown()) {
            FirstPairNavigationConfig.TOSScreen
        } else {
            FirstPairNavigationConfig.DeviceScreen
        },
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: FirstPairNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        FirstPairNavigationConfig.DeviceScreen -> deviceScreenFactory(
            componentContext = componentContext,
            onFinishConnect = invalidate,
            onHelpClick = { navigation.pushToFront(FirstPairNavigationConfig.HelpScreen) },
            onBack = {
                navigation.popOr(onBack::invoke)
            }
        )

        FirstPairNavigationConfig.HelpScreen -> connectionGuideFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) },
            onUsbConnected = invalidate
        )

        FirstPairNavigationConfig.TOSScreen -> tosScreenFactory(
            componentContext = componentContext,
            onApply = {
                navigation.pushToFront(FirstPairNavigationConfig.DeviceScreen)
            }
        )
    }
}
