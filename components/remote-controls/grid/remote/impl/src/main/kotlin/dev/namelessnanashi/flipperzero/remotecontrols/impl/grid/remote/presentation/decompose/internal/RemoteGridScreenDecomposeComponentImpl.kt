package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.internal

import androidx.compose.runtime.Composable
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.RemoteGridScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model.ServerRemoteControlParam
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.composable.RemoteGridComposable
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.RemoteGridComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.ThemeStatusBarIconStyleProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, RemoteGridScreenDecomposeComponent.Factory::class)
class RemoteGridScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted param: ServerRemoteControlParam,
    @Assisted onBack: DecomposeOnBackParameter,
    @Assisted onSaveKey: (NotSavedFlipperKey) -> Unit,
    remoteGridComponentFactory: RemoteGridComponent.Factory,
    flipperDispatchDialogApiFactory: FlipperDispatchDialogApi.Factory,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    dataStore: DataStore<Settings>
) : RemoteGridScreenDecomposeComponent(componentContext) {
    private val gridComponent = remoteGridComponentFactory.invoke(
        componentContext = childContext("GridComponent"),
        param = param,
        onBack = onBack,
        onSaveKey = onSaveKey
    )
    private val flipperDispatchDialogApi = flipperDispatchDialogApiFactory.invoke(onBack = onBack)
    private val themeStatusBarIconStyleProvider = ThemeStatusBarIconStyleProvider(dataStore)

    @Composable
    override fun Render() {
        RemoteGridComposable(
            remoteGridComponent = gridComponent,
            errorsRenderer = errorsRenderer,
            flipperDispatchDialogApi = flipperDispatchDialogApi
        )
    }

    override fun isStatusBarIconLight(systemIsDark: Boolean): Boolean {
        return themeStatusBarIconStyleProvider.isStatusBarIconLight(systemIsDark)
    }
}
