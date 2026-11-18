package dev.namelessnanashi.flipperzero.filemanager.upload.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.instancekeeper.getOrCreate
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import dev.namelessnanashi.flipperzero.filemanager.upload.api.UploaderDecomposeComponent
import dev.namelessnanashi.flipperzero.filemanager.upload.impl.composable.UploadingComposable
import dev.namelessnanashi.flipperzero.filemanager.upload.impl.viewmodel.UploadViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import okio.Path
import dev.zacsweers.metro.Provider

@ContributesAssistedFactory(AppGraph::class, UploaderDecomposeComponent.Factory::class)
class UploaderDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val uploadViewModelFactory: Provider<UploadViewModel>
) : UploaderDecomposeComponent, ComponentContext by componentContext {
    private val uploadViewModel = instanceKeeper.getOrCreate {
        uploadViewModelFactory.invoke()
    }

    override val state = uploadViewModel.state
    override val speedState = uploadViewModel.speedState

    override fun onCancel() = uploadViewModel.onCancel()

    override fun uploadRaw(
        folderPath: Path,
        fileName: String,
        content: ByteArray
    ) = uploadViewModel.uploadRaw(
        folderPath = folderPath,
        fileName = fileName,
        content = content
    )

    override fun tryUpload(
        folderPath: Path,
        contents: List<DeeplinkContent>
    ) = uploadViewModel.tryUpload(
        folderPath = folderPath,
        contents = contents
    )

    @Composable
    override fun Render(
        state: UploaderDecomposeComponent.State,
        speedState: Long?,
        onCancelClick: () -> Unit,
        modifier: Modifier
    ) {
        when (val localState = state) {
            is UploaderDecomposeComponent.State.Uploading -> {
                UploadingComposable(
                    state = localState,
                    speed = speedState,
                    onCancel = onCancelClick,
                    modifier = modifier
                )
            }

            UploaderDecomposeComponent.State.Cancelled,
            UploaderDecomposeComponent.State.Error,
            UploaderDecomposeComponent.State.Pending,
            is UploaderDecomposeComponent.State.Uploaded -> Unit
        }
    }
}
