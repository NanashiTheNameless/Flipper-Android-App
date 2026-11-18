package dev.namelessnanashi.flipperzero.infrared.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pushNew
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.infrared.api.InfraredDecomposeComponent
import dev.namelessnanashi.flipperzero.infrared.api.InfraredEditorDecomposeComponent
import dev.namelessnanashi.flipperzero.infrared.impl.model.InfraredNavigationConfig
import dev.namelessnanashi.flipperzero.keyedit.api.KeyEditDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.api.LocalGridScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, InfraredDecomposeComponent.Factory::class)
class InfraredDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val keyPath: FlipperKeyPath,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val infraredViewFactory: InfraredViewDecomposeComponentImpl.Factory,
    private val infraredEditorFactory: InfraredEditorDecomposeComponent.Factory,
    private val editorKeyFactory: KeyEditDecomposeComponent.Factory,
    private val savedGridFactory: LocalGridScreenDecomposeComponent.Factory
) : InfraredDecomposeComponent<InfraredNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<InfraredNavigationConfig, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = InfraredNavigationConfig.serializer(),
            initialConfiguration = InfraredNavigationConfig.RemoteControl(keyPath),
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: InfraredNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is InfraredNavigationConfig.View -> infraredViewFactory(
            componentContext = componentContext,
            keyPath = config.keyPath,
            navigation = navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is InfraredNavigationConfig.Edit -> infraredEditorFactory(
            componentContext = componentContext,
            keyPath = config.keyPath,
            onBack = { navigation.popOr(onBack::invoke) }
        )

        is InfraredNavigationConfig.Rename -> editorKeyFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) },
            flipperKeyPath = config.keyPath,
            title = null
        )

        is InfraredNavigationConfig.RemoteControl -> savedGridFactory.invoke(
            componentContext = componentContext,
            keyPath = keyPath,
            onBack = { navigation.popOr(onBack::invoke) },
            onCallback = {
                when (it) {
                    LocalGridScreenDecomposeComponent.Callback.UiFileNotFound -> {
                        navigation.replaceCurrent(InfraredNavigationConfig.View(config.keyPath))
                    }
                    is LocalGridScreenDecomposeComponent.Callback.ViewRemoteInfo -> {
                        navigation.replaceCurrent(InfraredNavigationConfig.View(it.keyPath))
                    }

                    is LocalGridScreenDecomposeComponent.Callback.Rename -> {
                        navigation.pushNew(InfraredNavigationConfig.Rename(it.keyPath))
                    }

                    LocalGridScreenDecomposeComponent.Callback.Deleted -> navigation.popOr(onBack::invoke)
                }
            }
        )
    }
}
