package dev.namelessnanashi.flipperzero.settings.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.debug.api.StressTestDecomposeComponent
import dev.namelessnanashi.flipperzero.filemanager.main.api.FileManagerDecomposeComponent
import dev.namelessnanashi.flipperzero.settings.api.SettingsDecomposeComponent
import dev.namelessnanashi.flipperzero.settings.impl.model.SettingsNavigationConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, SettingsDecomposeComponent.Factory::class)
class SettingsDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val fileManagerComponentFactory: FileManagerDecomposeComponent.Factory,
    private val mainComponentFactory: MainScreenDecomposeComponent.Factory,
    private val stressTestFactory: StressTestDecomposeComponent.Factory
) : SettingsDecomposeComponent<SettingsNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<SettingsNavigationConfig, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = SettingsNavigationConfig.serializer(),
            initialConfiguration = SettingsNavigationConfig.Main,
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: SettingsNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        SettingsNavigationConfig.FileManager -> fileManagerComponentFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        SettingsNavigationConfig.Main -> mainComponentFactory(
            componentContext,
            navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        SettingsNavigationConfig.StressTest -> stressTestFactory(componentContext)
    }
}
