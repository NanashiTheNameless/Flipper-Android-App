package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.toFapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import dev.namelessnanashi.flipperzero.infrared.editor.core.parser.InfraredKeyParser
import dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model.ServerRemoteControlParam
import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.data.pages.PagesRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Suppress("LongParameterList")
class RemoteGridViewModel @AssistedInject constructor(
    private val pagesRepository: PagesRepository,
    @Assisted private val param: ServerRemoteControlParam,
    @Assisted private val onCallback: (Callback) -> Unit,
) : DecomposeViewModel(), LogTagProvider {
    override val TAG: String = "GridViewModel"

    private val json: Json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun tryLoad() {
        viewModelScope.launch {
            val pagesLayout =
                pagesRepository.fetchDefaultPageLayout(ifrFileId = param.infraredFileId)
                    .onFailure { _state.emit(State.Error(it.toFapHubError())) }
                    .onFailure { throwable -> error(throwable) { "#tryLoad could not load ui model" } }
                    .getOrNull() ?: return@launch
            val pagesLayoutRaw = json.encodeToString(pagesLayout)

            val remotesRaw = pagesRepository.fetchKeyContent(param.infraredFileId)
                .onFailure { _state.emit(State.Error(it.toFapHubError())) }
                .onFailure { throwable -> error(throwable) { "#tryLoad could not load key content" } }
                .getOrNull()
                .orEmpty()

            val callback = Callback.ContentLoaded(
                infraredContent = remotesRaw,
                uiContent = pagesLayoutRaw
            )
            onCallback.invoke(callback)
            _state.emit(
                value = State.Loaded(
                    pagesLayout = pagesLayout,
                    remotes = remotesRaw
                        .let(FlipperFileFormat::fromFileContent)
                        .let(InfraredKeyParser::mapParsedKeyToInfraredRemotes)
                        .toImmutableList(),
                    remotesRaw = remotesRaw,
                    isDownloadedOnFlipper = true
                )
            )
        }
    }

    fun getRawRemotesContent(): String? {
        val state = state.value as? State.Loaded ?: return null
        return state.remotesRaw
    }

    fun getRawPagesContent(): String? {
        val state = state.value as? State.Loaded ?: return null
        return json.encodeToString(state.pagesLayout)
    }

    init {
        tryLoad()
    }

    sealed interface State {
        data object Loading : State
        data class Error(val throwable: FapHubError) : State
        data class Loaded(
            val pagesLayout: PagesLayout,
            val remotes: ImmutableList<InfraredRemote>,
            val remotesRaw: String,
            val isDownloadedOnFlipper: Boolean
        ) : State
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            param: ServerRemoteControlParam,
            onCallback: (Callback) -> Unit,
        ): RemoteGridViewModel
    }

    sealed interface Callback {
        data class ContentLoaded(
            val infraredContent: String,
            val uiContent: String
        ) : Callback
    }
}
