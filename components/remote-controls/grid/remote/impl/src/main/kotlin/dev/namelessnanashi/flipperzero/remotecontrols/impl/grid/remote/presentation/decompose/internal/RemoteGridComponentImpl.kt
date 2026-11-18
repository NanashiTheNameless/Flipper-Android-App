package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.internal

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperFile
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.remotecontrols.api.DispatchSignalApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.SaveTempSignalApi
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model.ServerRemoteControlParam
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.RemoteGridComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.mapping.GridComponentStateMapper
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.viewmodel.ConnectionViewModel
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.viewmodel.RemoteGridViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, RemoteGridComponent.Factory::class)
class RemoteGridComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted override val param: ServerRemoteControlParam,
    @Assisted private val onBack: DecomposeOnBackParameter,
    @Assisted private val onSaveKey: (NotSavedFlipperKey) -> Unit,
    createRemoteGridViewModel: RemoteGridViewModel.Factory,
    createSaveTempSignalApi: Provider<SaveTempSignalApi>,
    createDispatchSignalApi: Provider<DispatchSignalApi>,
    createConnectionViewModel: Provider<ConnectionViewModel>
) : RemoteGridComponent, ComponentContext by componentContext {
    private val saveTempSignalApi = instanceKeeper.getOrCreate(
        key = "GridComponent_saveSignalViewModel_${param.key}",
        factory = {
            createSaveTempSignalApi.invoke()
        }
    )
    private val connectionViewModel = instanceKeeper.getOrCreate(
        key = "GridComponent_connectionViewModel_${param.key}",
        factory = {
            createConnectionViewModel.invoke()
        }
    )
    private val dispatchSignalApi = instanceKeeper.getOrCreate(
        key = "GridComponent_dispatchSignalViewModel_${param.key}",
        factory = {
            createDispatchSignalApi.invoke()
        }
    )
    private val remoteGridViewModel = instanceKeeper.getOrCreate(
        key = "GridComponent_gridFeature_${param.key}",
        factory = {
            createRemoteGridViewModel.invoke(
                param = param,
                onCallback = { callback ->
                    when (callback) {
                        is RemoteGridViewModel.Callback.ContentLoaded -> {
                            saveTempSignalApi.saveFiles(
                                SaveTempSignalApi.FileDesc(
                                    textContent = callback.infraredContent,
                                    nameWithExtension = param.nameWithExtension,
                                    extFolderPath = param.extTempFolderPath
                                ),
                                SaveTempSignalApi.FileDesc(
                                    textContent = callback.uiContent,
                                    nameWithExtension = param.uiFileNameWithExtension,
                                    extFolderPath = param.extTempFolderPath
                                )
                            )
                        }
                    }
                }
            )
        }
    )

    override fun model(coroutineScope: CoroutineScope) = combine(
        saveTempSignalApi.state,
        remoteGridViewModel.state,
        dispatchSignalApi.state,
        connectionViewModel.state,
        transform = { saveState, gridState, dispatchState, connectionState ->
            GridComponentStateMapper.map(
                saveState = saveState,
                gridState = gridState,
                dispatchState = dispatchState,
                connectionState = connectionState
            )
        }
    ).stateIn(coroutineScope, SharingStarted.Eagerly, RemoteGridComponent.Model.Loading())

    override fun dismissDialog() {
        dispatchSignalApi.dismissBusyDialog()
    }

    private fun FlipperFilePath.toNonTempPath() = copy(folder = folder.replace("/temp", ""))

    override fun save() {
        val rawRemotes = remoteGridViewModel.getRawRemotesContent() ?: return
        val rawUi = remoteGridViewModel.getRawPagesContent() ?: return

        val notSavedFlipperFile = NotSavedFlipperKey(
            mainFile = NotSavedFlipperFile(
                FlipperFilePath(
                    folder = param.extTempFolderPath,
                    nameWithExtension = param.nameWithExtension
                ).toNonTempPath(),
                content = FlipperKeyContent.RawData(rawRemotes.toByteArray())
            ),
            additionalFiles = listOf(
                NotSavedFlipperFile(
                    path = FlipperFilePath(
                        folder = param.extTempFolderPath,
                        nameWithExtension = param.uiFileNameWithExtension
                    ).toNonTempPath(),
                    content = FlipperKeyContent.RawData(rawUi.toByteArray())
                )
            ),
            notes = null
        )
        onSaveKey.invoke(notSavedFlipperFile)
    }

    private fun onButtonClick(identifier: IfrKeyIdentifier, isOneTime: Boolean) {
        val gridLoadedState = (remoteGridViewModel.state.value as? RemoteGridViewModel.State.Loaded)
            ?: return
        dispatchSignalApi.dispatch(
            identifier = identifier,
            remotes = gridLoadedState.remotes,
            isOneTime = isOneTime,
            ffPath = FlipperFilePath(
                folder = param.extTempFolderPath,
                nameWithExtension = param.nameWithExtension
            )
        )
    }

    override fun onButtonClick(identifier: IfrKeyIdentifier) {
        onButtonClick(identifier, true)
    }

    override fun onButtonLongClick(identifier: IfrKeyIdentifier) {
        onButtonClick(identifier, false)
    }

    override fun onButtonRelease() {
        dispatchSignalApi.stopEmulate()
    }

    override fun tryLoad() = remoteGridViewModel.tryLoad()
    override fun pop() = onBack.invoke()
}
