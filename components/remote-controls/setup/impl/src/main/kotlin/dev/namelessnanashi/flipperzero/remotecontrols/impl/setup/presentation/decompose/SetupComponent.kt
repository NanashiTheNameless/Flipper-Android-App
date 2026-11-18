package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.decompose

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.IfrFileModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalResponseModel
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.infrared.api.InfraredConnectionApi.InfraredEmulateState
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.api.SetupScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.StateFlow
import uk.kulikov.metro.assisted.AssistedKey

interface SetupComponent {
    fun model(coroutineScope: CoroutineScope): StateFlow<Model>

    val remoteFoundFlow: Flow<IfrFileModel>
    val lastEmulatedSignal: StateFlow<SignalResponse?>
    val param: SetupScreenDecomposeComponent.Param

    fun onBackClick()

    fun onSuccessClick()
    fun onFailedClick()
    fun onSkipClicked()
    fun dispatchSignal()
    fun forgetLastEmulatedSignal()

    fun dismissDialog()

    fun tryLoad()

    fun onFileFound(ifrFileModel: IfrFileModel)

    sealed interface Model {
        data class Loading(val progress: Float) : Model
        data class Loaded(
            val response: SignalResponseModel,
            val flipperDialog: FlipperDispatchDialogApi.DialogType? = null,
            val emulatedKeyIdentifier: IfrKeyIdentifier?,
            val connectionState: InfraredEmulateState
        ) : Model {
            val isSyncing: Boolean = listOf(
                InfraredEmulateState.CONNECTING,
                InfraredEmulateState.SYNCING
            ).contains(connectionState)
            val isConnected = connectionState != InfraredEmulateState.NOT_CONNECTED
        }

        data class Error(val throwable: FapHubError) : Model
    }

    interface Factory {
        fun createSetupComponent(
            componentContext: ComponentContext,
            param: SetupScreenDecomposeComponent.Param,
            @AssistedKey("onBackClick") onBack: DecomposeOnBackParameter,
            @AssistedKey("onIfrFileFound") onIrFileReady: (Long, String) -> Unit
        ): SetupComponent
    }
}
