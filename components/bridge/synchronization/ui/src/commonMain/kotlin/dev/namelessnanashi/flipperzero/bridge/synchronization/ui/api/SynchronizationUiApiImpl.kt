package dev.namelessnanashi.flipperzero.bridge.synchronization.ui.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.composable.ComposableItemSynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.model.ItemSynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel.ItemSynchronizationStateMapper
import dev.namelessnanashi.flipperzero.bridge.synchronization.ui.viewmodel.SynchronizationStateViewModel
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesBinding(AppGraph::class)
class SynchronizationUiApiImpl @Inject constructor(
    private val synchronizationStateViewModelProvider: Provider<SynchronizationStateViewModel>
) : SynchronizationUiApi {
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
