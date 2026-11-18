package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.presentation.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import dev.namelessnanashi.flipperzero.infrared.editor.core.parser.InfraredKeyParser
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyStateHelperApi
import dev.namelessnanashi.flipperzero.keyscreen.model.FavoriteState
import dev.namelessnanashi.flipperzero.keyscreen.model.KeyScreenState
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.serialization.json.Json

class LocalGridViewModel @AssistedInject constructor(
    @Assisted private val keyPath: FlipperKeyPath,
    keyStateHelperApi: KeyStateHelperApi.Builder,
) : DecomposeViewModel(), LogTagProvider {
    override val TAG: String = "LocalGridViewModel"
    private val json: Json = Json {
        prettyPrint = false
        ignoreUnknownKeys = true
        isLenient = true
    }

    private val keyStateHelper = keyStateHelperApi.build(keyPath, viewModelScope)

    val state = keyStateHelper.getKeyScreenState()
        .map {
            when (it) {
                is KeyScreenState.Error -> State.Error
                KeyScreenState.InProgress -> State.Loading
                is KeyScreenState.Ready -> {
                    val keyPath = it.flipperKey.getKeyPath()
                    val pagesLayout = it.flipperKey.additionalFiles.firstNotNullOfOrNull { fFile ->
                        val text = fFile.content.openStream().reader().readText()
                        runCatching {
                            json.decodeFromString<PagesLayout>(text)
                        }.getOrNull()
                    }
                    val remotes = it.flipperKey.keyContent
                        .openStream()
                        .reader()
                        .readText()
                        .let(FlipperFileFormat::fromFileContent)
                        .let(InfraredKeyParser::mapParsedKeyToInfraredRemotes)
                        .toImmutableList()
                    if (pagesLayout == null) {
                        State.Error
                    } else {
                        State.Loaded(
                            pagesLayout = pagesLayout,
                            remotes = remotes,
                            keyPath = keyPath,
                            isFavorite = it.favoriteState == FavoriteState.FAVORITE
                        )
                    }
                }
            }
        }.stateIn(viewModelScope, SharingStarted.Eagerly, State.Loading)

    fun onRename(onEndAction: (FlipperKeyPath) -> Unit) = keyStateHelper.onOpenEdit(onEndAction)

    fun onDelete(onEndAction: () -> Unit) = keyStateHelper.onDelete(onEndAction)

    fun toggleFavorite() {
        val state = keyStateHelper.getKeyScreenState().value
        val readyState = state as? KeyScreenState.Ready ?: return
        val isFavorite = readyState.favoriteState == FavoriteState.FAVORITE
        keyStateHelper.setFavorite(!isFavorite)
    }

    sealed interface State {
        data object Loading : State
        data object Error : State
        data class Loaded(
            val pagesLayout: PagesLayout,
            val remotes: ImmutableList<InfraredRemote>,
            val keyPath: FlipperKeyPath,
            val isFavorite: Boolean
        ) : State
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            keyPath: FlipperKeyPath,
        ): LocalGridViewModel
    }
}
