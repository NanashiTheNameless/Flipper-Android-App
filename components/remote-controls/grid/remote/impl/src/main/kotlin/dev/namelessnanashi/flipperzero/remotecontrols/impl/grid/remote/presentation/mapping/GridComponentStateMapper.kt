package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.mapping

import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.DispatchSignalApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi.Companion.toDialogType
import dev.namelessnanashi.flipperzero.remotecontrols.api.SaveTempSignalApi
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.RemoteGridComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.viewmodel.RemoteGridViewModel

internal object GridComponentStateMapper {
    fun map(
        saveState: SaveTempSignalApi.State,
        gridState: RemoteGridViewModel.State,
        dispatchState: DispatchSignalApi.State,
        connectionState: InfraredConnectionApi.InfraredEmulateState
    ): RemoteGridComponent.Model = when (gridState) {
        is RemoteGridViewModel.State.Error -> RemoteGridComponent.Model.Error(gridState.throwable)
        is RemoteGridViewModel.State.Loaded -> {
            when (saveState) {
                is SaveTempSignalApi.State.Uploading,
                SaveTempSignalApi.State.Uploaded,
                SaveTempSignalApi.State.Pending -> {
                    RemoteGridComponent.Model.Loaded(
                        pagesLayout = gridState.pagesLayout,
                        remotes = gridState.remotes,
                        flipperDialog = dispatchState.toDialogType(),
                        emulatedKey = (dispatchState as? DispatchSignalApi.State.Emulating)?.ifrKeyIdentifier,
                        saveState = saveState,
                        connectionState = connectionState
                    )
                }
            }
        }

        RemoteGridViewModel.State.Loading -> RemoteGridComponent.Model.Loading(0f)
    }
}
