package dev.namelessnanashi.flipperzero.filemanager.rename.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.FileType
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.filemanager.rename.api.RenameDecomposeComponent
import dev.namelessnanashi.flipperzero.filemanager.rename.impl.viewmodel.RenameViewModel
import dev.namelessnanashi.flipperzero.filemanager.ui.components.name.NameDialog
import dev.namelessnanashi.flipperzero.filemanager.util.constant.FileManagerConstants
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import flipperapp.components.filemngr.rename.impl.generated.resources.fmr_create_file_allowed_chars
import flipperapp.components.filemngr.rename.impl.generated.resources.fmr_create_file_folder_btn
import flipperapp.components.filemngr.rename.impl.generated.resources.fmr_create_file_title
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import okio.Path
import org.jetbrains.compose.resources.stringResource
import dev.zacsweers.metro.Provider
import flipperapp.components.filemngr.rename.impl.generated.resources.Res as FMR

@ContributesAssistedFactory(AppGraph::class, RenameDecomposeComponent.Factory::class)
class RenameDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted val renamedCallback: RenamedCallback,
    renameViewModelProvider: Provider<RenameViewModel>
) : RenameDecomposeComponent(componentContext) {
    private val renameViewModel = instanceKeeper.getOrCreate {
        renameViewModelProvider.invoke()
    }

    override fun startRename(fullPath: Path, type: FileType) {
        renameViewModel.startRename(fullPath, type)
    }

    @Composable
    override fun Render() {
        val state by renameViewModel.state.collectAsState()
        LaunchedEffect(renameViewModel) {
            renameViewModel.event
                .onEach { event ->
                    when (event) {
                        is RenameViewModel.Event.Renamed -> {
                            renamedCallback.invoke(event.oldFullPath, event.newFullPath)
                        }
                    }
                }.launchIn(this)
        }
        when (val localState = state) {
            RenameViewModel.State.Pending -> Unit
            is RenameViewModel.State.Renaming -> {
                NameDialog(
                    value = localState.name,
                    title = stringResource(FMR.string.fmr_create_file_title),
                    buttonText = stringResource(FMR.string.fmr_create_file_folder_btn),
                    subtitle = stringResource(
                        FMR.string.fmr_create_file_allowed_chars,
                        FileManagerConstants.FILE_NAME_AVAILABLE_CHARACTERS
                    ),
                    onFinish = renameViewModel::onConfirm,
                    isError = !localState.isValid,
                    isEnabled = !localState.isRenaming,
                    needShowOptions = localState.needShowOptions,
                    onTextChange = renameViewModel::onNameChange,
                    onDismissRequest = renameViewModel::dismiss,
                    onOptionSelect = renameViewModel::onOptionSelected,
                    options = localState.options,
                    isLoading = localState.isRenaming
                )
            }
        }
    }
}
