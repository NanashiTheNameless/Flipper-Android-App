package dev.namelessnanashi.flipperzero.nfceditor.impl.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyedit.api.KeyEditDecomposeComponent
import dev.namelessnanashi.flipperzero.nfceditor.api.NfcEditorDecomposeComponent
import dev.namelessnanashi.flipperzero.nfceditor.impl.model.NfcEditorNavigationConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, NfcEditorDecomposeComponent.Factory::class)
class NfcEditorDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val flipperKeyPath: FlipperKeyPath,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val saveDecomposeComponentFactory: KeyEditDecomposeComponent.Factory,
    private val nfcEditorDecomposeComponentFactory: NfcEditorScreenDecomposeComponentImpl.Factory
) : NfcEditorDecomposeComponent<NfcEditorNavigationConfig>(), ComponentContext by componentContext {

    override val stack: Value<ChildStack<NfcEditorNavigationConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = NfcEditorNavigationConfig.serializer(),
        initialConfiguration = NfcEditorNavigationConfig.NfcEditor(flipperKeyPath),
        handleBackButton = true,
        childFactory = ::child,
    )

    private fun child(
        config: NfcEditorNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is NfcEditorNavigationConfig.NfcEditor -> nfcEditorDecomposeComponentFactory(
            componentContext = componentContext,
            flipperKeyPath = config.flipperKeyPath,
            navigation = navigation,
            onBack = onBack
        )

        is NfcEditorNavigationConfig.Save -> saveDecomposeComponentFactory(
            componentContext = componentContext,
            onBack = { navigation.popOr(onBack::invoke) },
            notSavedFlipperKey = config.notSavedFlipperKey,
            title = config.title
        )
    }
}
