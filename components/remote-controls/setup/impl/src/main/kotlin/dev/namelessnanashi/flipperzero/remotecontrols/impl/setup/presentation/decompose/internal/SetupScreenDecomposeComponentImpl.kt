package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.decompose.internal

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.SetupScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.composable.SetupScreen
import dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.decompose.SetupComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, SetupScreenDecomposeComponent.Factory::class)
class SetupScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted param: SetupScreenDecomposeComponent.Param,
    @Assisted onBack: () -> Unit,
    @Assisted onIrFileReady: (Long, String) -> Unit,
    setupComponentFactory: SetupComponent.Factory,
    flipperDispatchDialogApiFactory: FlipperDispatchDialogApi.Factory,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
) : SetupScreenDecomposeComponent(componentContext) {
    private val setupComponent = setupComponentFactory.createSetupComponent(
        componentContext = childContext("SetupComponent"),
        param = param,
        onBack = onBack,
        onIrFileReady = onIrFileReady
    )
    private val flipperDispatchDialogApi = flipperDispatchDialogApiFactory.invoke(onBack = onBack)

    @Composable
    override fun Render() {
        SetupScreen(
            setupComponent = setupComponent,
            errorsRenderer = errorsRenderer,
            flipperDispatchDialogApi = flipperDispatchDialogApi
        )
    }
}
