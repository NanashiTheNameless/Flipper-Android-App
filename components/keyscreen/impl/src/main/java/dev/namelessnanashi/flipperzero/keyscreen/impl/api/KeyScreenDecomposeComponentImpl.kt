package dev.namelessnanashi.flipperzero.keyscreen.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.infrared.api.InfraredDecomposeComponent
import dev.namelessnanashi.flipperzero.keyedit.api.KeyEditDecomposeComponent
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.keyscreen.impl.model.KeyScreenNavigationConfig
import dev.namelessnanashi.flipperzero.nfceditor.api.NfcEditorDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, KeyScreenDecomposeComponent.Factory::class)
@Suppress("LongParameterList")
class KeyScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted keyPath: FlipperKeyPath,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val infraredKeyScreenFactory: InfraredDecomposeComponent.Factory,
    private val keyScreenViewFactory: KeyScreenViewDecomposeComponentImpl.Factory,
    private val keyEditFactory: KeyEditDecomposeComponent.Factory,
    private val nfcEditFactory: NfcEditorDecomposeComponent.Factory
) : KeyScreenDecomposeComponent<KeyScreenNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<KeyScreenNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = KeyScreenNavigationConfig.serializer(),
        initialConfiguration = KeyScreenNavigationConfig.Main(keyPath),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: KeyScreenNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is KeyScreenNavigationConfig.Main -> when {
            config.keyPath.path.keyType != FlipperKeyType.INFRARED ||
                config.keyPath.deleted ->
                keyScreenViewFactory(
                    componentContext = componentContext,
                    keyPath = config.keyPath,
                    navigation = navigation,
                    onBack = this::internalOnBack
                )

            else -> infraredKeyScreenFactory(
                componentContext = componentContext,
                keyPath = config.keyPath,
                onBack = this::internalOnBack
            )
        }

        is KeyScreenNavigationConfig.KeyEdit -> keyEditFactory(
            componentContext = componentContext,
            onBack = this::internalOnBack,
            flipperKeyPath = config.keyPath,
            title = null
        )

        is KeyScreenNavigationConfig.NfcEdit -> nfcEditFactory(
            componentContext = componentContext,
            flipperKeyPath = config.keyPath,
            onBack = this::internalOnBack
        )
    }

    private fun internalOnBack() = navigation.popOr(onBack::invoke)
}
