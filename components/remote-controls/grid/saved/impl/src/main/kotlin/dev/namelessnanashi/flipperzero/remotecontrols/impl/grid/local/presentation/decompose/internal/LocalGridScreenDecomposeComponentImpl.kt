package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.presentation.decompose.internal

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.datastore.core.DataStore
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import com.arkivanov.essenty.backhandler.BackCallback
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.api.LocalGridScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.composable.LocalGridComposable
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.presentation.decompose.LocalGridComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.ThemeStatusBarIconStyleProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.update
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, LocalGridScreenDecomposeComponent.Factory::class)
class LocalGridScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val keyPath: FlipperKeyPath,
    @Assisted onBack: DecomposeOnBackParameter,
    @Assisted private val onCallback: (Callback) -> Unit,
    localGridComponentFactory: LocalGridComponent.Factory,
    flipperDispatchDialogApiFactory: FlipperDispatchDialogApi.Factory,
    dataStore: DataStore<Settings>
) : LocalGridScreenDecomposeComponent(componentContext) {
    private val localGridComponent = localGridComponentFactory.invoke(
        componentContext = childContext("GridComponent_local"),
        keyPath = keyPath,
        onBack = onBack,
    )
    private val flipperDispatchDialogApi = flipperDispatchDialogApiFactory.invoke(onBack = onBack)

    private val isBackPressHandledFlow = MutableStateFlow(false)
    private val backCallback = BackCallback(false) { isBackPressHandledFlow.update { true } }
    private val themeStatusBarIconStyleProvider = ThemeStatusBarIconStyleProvider(dataStore)

    init {
        backHandler.register(backCallback)
    }

    @Composable
    override fun Render() {
        LocalGridComposable(
            localGridComponent = localGridComponent,
            flipperDispatchDialogApi = flipperDispatchDialogApi,
            onCallback = onCallback,
        )
    }

    override fun isStatusBarIconLight(systemIsDark: Boolean): Boolean {
        return themeStatusBarIconStyleProvider.isStatusBarIconLight(systemIsDark)
    }
}
