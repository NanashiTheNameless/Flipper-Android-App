package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.toFapButtonConfig
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapInstalledApi
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.ComposableInstalledTabScreen
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.offline.dialog.ComposableOfflineAppDialogBox
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapBatchUpdateButtonState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.InstalledFapsViewModel
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.RepairAppsViewModel
import dev.namelessnanashi.flipperzero.faphub.uninstallbutton.api.FapUninstallApi
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapInstalledApi>())
class FapInstalledApiImpl @Inject constructor(
    private val fapInstallationUIApi: FapInstallationUIApi,
    private val uninstallApi: FapUninstallApi,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    private val installedFapsViewModelProvider: Provider<InstalledFapsViewModel>,
    private val repairAppsViewModelProvider: Provider<RepairAppsViewModel>
) : FapInstalledApi {

    @Composable
    override fun getUpdatePendingCount(componentContext: ComponentContext): Int {
        val installedViewModel = componentContext.viewModelWithFactory(key = null) {
            installedFapsViewModelProvider.invoke()
        }
        val buttonStateFlow = remember { installedViewModel.getFapBatchUpdateButtonState() }
        val state by buttonStateFlow.collectAsState()
        return state.let {
            when (it) {
                FapBatchUpdateButtonState.Loading,
                FapBatchUpdateButtonState.NoUpdates,
                FapBatchUpdateButtonState.UpdatingInProgress,
                FapBatchUpdateButtonState.Offline -> 0

                is FapBatchUpdateButtonState.ReadyToUpdate -> it.count
            }
        }
    }

    @Composable
    @Suppress("NonSkippableComposable")
    override fun ComposableInstalledTab(
        componentContext: ComponentContext,
        onOpenFapItem: (uid: String) -> Unit
    ) {
        val installedViewModel = componentContext.viewModelWithFactory(key = null) {
            installedFapsViewModelProvider.invoke()
        }
        ComposableInstalledTabScreen(
            onOpenFapItem = onOpenFapItem,
            installationButton = { fapItem, modifier ->
                fapInstallationUIApi.ComposableButton(
                    config = fapItem?.toFapButtonConfig(),
                    modifier = modifier,
                    fapButtonSize = FapButtonSize.COMPACTED,
                    componentContext = componentContext
                )
            },
            uninstallButtonOffline = { offlineFapApp, modifier ->
                uninstallApi.ComposableFapUninstallButton(
                    modifier = modifier,
                    applicationUid = offlineFapApp.applicationUid
                ) {
                    ComposableOfflineAppDialogBox(offlineFapApp = offlineFapApp, modifier = it)
                }
            },
            uninstallButtonOnline = { fapItemShort, modifier ->
                uninstallApi.ComposableFapUninstallButton(
                    modifier = modifier,
                    fapItem = fapItemShort
                )
            },
            repairAppsViewModel = componentContext.viewModelWithFactory(key = null) {
                repairAppsViewModelProvider.invoke()
            },
            errorsRenderer = errorsRenderer,
            viewModel = installedViewModel
        )
    }
}
