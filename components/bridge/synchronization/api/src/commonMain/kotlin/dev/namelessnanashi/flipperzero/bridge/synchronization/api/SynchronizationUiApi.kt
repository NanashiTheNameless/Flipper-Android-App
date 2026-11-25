package dev.namelessnanashi.flipperzero.bridge.synchronization.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath

@Immutable
interface SynchronizationUiApi {
    @Composable
    fun RenderSynchronizationDialog()

    @Composable
    fun RenderSynchronizationStatus()

    fun showSynchronizationDialog()

    @Composable
    fun RenderSynchronizationState(
        componentContext: ComponentContext,
        keyPath: FlipperKeyPath,
        withText: Boolean
    )

    @Composable
    fun RenderSynchronizationState(
        synced: Boolean,
        synchronizationState: SynchronizationState,
        withText: Boolean
    )
}
