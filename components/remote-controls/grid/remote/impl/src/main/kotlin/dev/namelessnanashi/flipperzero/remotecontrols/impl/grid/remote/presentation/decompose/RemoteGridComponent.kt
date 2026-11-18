package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi.InfraredEmulateState
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import dev.namelessnanashi.flipperzero.keyedit.api.NotSavedFlipperKey
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.SaveTempSignalApi
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model.ServerRemoteControlParam
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import kotlinx.collections.immutable.ImmutableList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.StateFlow

interface RemoteGridComponent {
    val param: ServerRemoteControlParam

    fun model(coroutineScope: CoroutineScope): StateFlow<Model>

    fun onButtonClick(identifier: IfrKeyIdentifier)
    fun onButtonLongClick(identifier: IfrKeyIdentifier)
    fun onButtonRelease()
    fun tryLoad()

    fun pop()

    fun dismissDialog()

    fun save()

    sealed interface Model {
        data class Loading(
            val progress: Float = 0f,
        ) : Model

        data class Loaded(
            val pagesLayout: PagesLayout,
            val remotes: ImmutableList<InfraredRemote>,
            val flipperDialog: FlipperDispatchDialogApi.DialogType? = null,
            val emulatedKey: IfrKeyIdentifier? = null,
            val saveState: SaveTempSignalApi.State,
            val connectionState: InfraredEmulateState
        ) : Model {
            val isConnected = connectionState != InfraredEmulateState.NOT_CONNECTED
            val isSavingFiles = saveState is SaveTempSignalApi.State.Uploading
            val saveProgressOrNull = (saveState as? SaveTempSignalApi.State.Uploading)
                ?.progressPercent
                ?.times(other = 100)
                ?.toInt()
                ?.coerceIn(minimumValue = 0, maximumValue = 100)
        }

        data class Error(val throwable: FapHubError) : Model

        val isFilesSaved: Boolean
            get() = this is Loaded && !this.isSavingFiles
    }

    fun interface Factory {
        fun invoke(
            componentContext: ComponentContext,
            param: ServerRemoteControlParam,
            onBack: DecomposeOnBackParameter,
            onSaveKey: (NotSavedFlipperKey) -> Unit
        ): RemoteGridComponent
    }
}
