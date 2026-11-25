package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.api

import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable.ComposableSynchronizationStatus
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.serialspeed.api.FSpeedFeatureApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable.ComposableItemSynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable.ComposableSynchronizationDialog
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.model.ItemSynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel.ItemSynchronizationStateMapper
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel.SynchronizationStateViewModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class)
class SynchronizationUiApiImpl @Inject constructor(
    private val synchronizationStateViewModelProvider: Provider<SynchronizationStateViewModel>,
    private val synchronizationApi: SynchronizationApi,
    private val featureProvider: FFeatureProvider,
    private val orchestrator: FDeviceOrchestrator
) : SynchronizationUiApi {
    private val speedFlow = featureProvider.get<FSpeedFeatureApi>().flatMapLatest { status ->
        (status as? FFeatureStatus.Supported<FSpeedFeatureApi>)?.featureApi?.getSpeed() ?: flowOf(null)
    }
    private val dialogDismissed = MutableStateFlow(false)

    override fun showSynchronizationDialog() {
        dialogDismissed.value = false
    }

    @Composable
    override fun RenderSynchronizationDialog() {
        val state by synchronizationApi.getSynchronizationState().collectAsState()
        val dismissed by dialogDismissed.collectAsState()
        val connection by orchestrator.getState().collectAsState()
        val inProgress = state is SynchronizationState.InProgress &&
            (state as SynchronizationState.InProgress).progress > 0f &&
            connection is FDeviceConnectStatus.Connected
        LaunchedEffect(inProgress) {
            if (!inProgress) dialogDismissed.value = false
        }
        if (inProgress && !dismissed) {
            val speed by speedFlow.collectAsState(null)
            val scope = rememberCoroutineScope()
            ComposableSynchronizationDialog(
                inProgressState = state as SynchronizationState.InProgress,
                speed = speed,
                onDismiss = { dialogDismissed.value = true },
                onCancel = { scope.launch { synchronizationApi.stop() } }
            )
        }
    }

    @Composable
    override fun RenderSynchronizationStatus() {
        val state by synchronizationApi.getSynchronizationState().collectAsState()
        val speed by speedFlow.collectAsState(null)
        ComposableSynchronizationStatus(state, speed, ::showSynchronizationDialog)
    }

    @Composable
    override fun RenderSynchronizationState(
        componentContext: ComponentContext,
        keyPath: FlipperKeyPath,
        withText: Boolean,
    ) {
        val synchronizationViewModel = componentContext.viewModelWithFactory(null) {
            synchronizationStateViewModelProvider.invoke()
        }
        val state by synchronizationViewModel.getSynchronizationState(keyPath).collectAsState(
            initial = ItemSynchronizationState.NOT_SYNCHRONIZED
        )
        ComposableItemSynchronizationState(state, withText)
    }

    @Composable
    override fun RenderSynchronizationState(
        synced: Boolean,
        synchronizationState: SynchronizationState,
        withText: Boolean
    ) {
        ComposableItemSynchronizationState(
            itemSynchronizationState = ItemSynchronizationStateMapper.getItemSynchronizedState(
                synchronized = synced,
                synchronizationState
            ),
            withText = withText
        )
    }
}
