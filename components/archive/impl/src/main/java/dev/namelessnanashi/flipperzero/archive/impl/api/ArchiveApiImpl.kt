package dev.namelessnanashi.flipperzero.archive.impl.api

import androidx.compose.foundation.lazy.LazyListScope
import dev.namelessnanashi.flipperzero.archive.api.ArchiveApi
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableKeysGrid
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppGraph::class)
class ArchiveApiImpl @Inject constructor(
    private val synchronizationUiApi: SynchronizationUiApi
) : ArchiveApi {
    override fun LazyListScope.ComposableKeysGridWithSynchronization(
        keys: List<FlipperKey>,
        synchronizationState: SynchronizationState,
        onKeyOpen: (FlipperKeyPath) -> Unit
    ) {
        ComposableKeysGrid(
            keys,
            synchronizationUiApi,
            synchronizationState,
            onKeyOpen
        )
    }
}
