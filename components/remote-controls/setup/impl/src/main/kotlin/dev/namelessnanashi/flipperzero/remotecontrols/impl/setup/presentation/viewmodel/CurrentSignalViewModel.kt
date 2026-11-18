package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.toFapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.FlipperInfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalRequestModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalRequestModel.SignalResultData
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalResponseModel
import dev.namelessnanashi.flipperzero.remotecontrols.api.SetupScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class CurrentSignalViewModel @AssistedInject constructor(
    private val infraredBackendApi: FlipperInfraredBackendApi,
    @Assisted private val param: SetupScreenDecomposeComponent.Param,
    @Assisted private val onLoaded: (SignalResponseModel) -> Unit
) : DecomposeViewModel(), LogTagProvider {
    override val TAG: String = "CurrentSignalViewModel"
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun load(
        successResults: List<SignalResultData>,
        failedResults: List<SignalResultData>,
        skippedResults: List<SignalResultData>
    ) = viewModelScope.launch {
        _state.emit(State.Loading)
        val result = runCatching {
            val request = SignalRequestModel(
                successResults = successResults,
                failedResults = failedResults,
                skippedResults = skippedResults,
                brandId = param.brandId,
            )
            infraredBackendApi.getSignal(request)
        }
        result
            .onFailure { _state.emit(State.Error(it.toFapHubError())) }
            .onFailure { throwable -> error(throwable) { "#tryLoad could not load signal model" } }
            .onSuccess { _state.emit(State.Loaded(it)) }
            .onSuccess(onLoaded)
    }

    init {
        load(emptyList(), emptyList(), emptyList())
    }

    sealed interface State {
        data object Loading : State
        data class Error(val throwable: FapHubError) : State
        data class Loaded(val response: SignalResponseModel) : State
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            param: SetupScreenDecomposeComponent.Param,
            onLoaded: (SignalResponseModel) -> Unit
        ): CurrentSignalViewModel
    }
}
