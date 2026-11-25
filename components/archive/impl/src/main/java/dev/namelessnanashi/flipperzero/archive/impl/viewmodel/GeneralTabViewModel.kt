package dev.namelessnanashi.flipperzero.archive.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteSaveState
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteApp
import dev.namelessnanashi.flipperzero.archive.impl.model.builtInFavoriteApps
import dev.namelessnanashi.flipperzero.archive.impl.model.favoriteAppsFromManifests
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import kotlinx.coroutines.CancellationException
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

class GeneralTabViewModel @Inject constructor(
    private val simpleKeyApi: SimpleKeyApi,
    private val favoriteApi: FavoriteApi,
    private val synchronizationApi: SynchronizationApi,
    fapManifestApi: FapManifestApi,
) : DecomposeViewModel() {
    val favoriteSaveState = MutableStateFlow(FavoriteSaveState.IDLE)
    val favoriteApps = MutableStateFlow<ImmutableList<FavoriteApp>>(builtInFavoriteApps.toImmutableList())
    val favoriteAppsLoading = MutableStateFlow(true)
    private val keys = MutableStateFlow<ImmutableList<FlipperKey>>(persistentListOf())
    private val favoritePaths = MutableStateFlow<ImmutableList<FlipperKeyPath>>(persistentListOf())
    private val favoriteKeys = MutableStateFlow<ImmutableList<FlipperKey>>(persistentListOf())
    private val synchronizationState =
        MutableStateFlow<SynchronizationState>(SynchronizationState.NotStarted)

    init {
        fapManifestApi.getManifestFlow().onEach { state ->
            val loaded = state as? FapManifestState.Loaded
            favoriteApps.value = favoriteAppsFromManifests(loaded?.items.orEmpty()).toImmutableList()
            favoriteAppsLoading.value = loaded?.inProgress == true
        }.launchIn(viewModelScope)
        viewModelScope.launch {
            simpleKeyApi.getExistKeysAsFlow(null)
                .combine(favoriteApi.getFavoritesFlow()) { keyList, favoriteKeysList ->
                    val favoriteKeyPaths = favoriteKeysList.map { it.path }.toSet()
                    val keysExceptFavorite =
                        keyList.filterNot { favoriteKeyPaths.contains(it.path) }
                    keys.emit(keysExceptFavorite.toImmutableList())
                    favoriteKeys.emit(favoriteKeysList.toImmutableList())
                }.launchIn(viewModelScope)
            favoriteApi.getFavoritePathsFlow().onEach {
                favoritePaths.value = it.toImmutableList()
            }.launchIn(viewModelScope)
            synchronizationApi.getSynchronizationState().onEach {
                synchronizationState.emit(it)
            }.launchIn(viewModelScope)
        }
    }

    fun getKeys(): StateFlow<ImmutableList<FlipperKey>?> = keys
    fun getFavoriteKeys(): StateFlow<ImmutableList<FlipperKey>> = favoriteKeys
    fun getFavoritePaths(): StateFlow<ImmutableList<FlipperKeyPath>> = favoritePaths
    fun getSynchronizationState(): StateFlow<SynchronizationState> = synchronizationState

    fun refresh() {
        synchronizationApi.startSynchronization(force = true)
    }

    fun saveFavorites(paths: List<FlipperKeyPath>, onSaved: () -> Unit) {
        if (favoriteSaveState.value == FavoriteSaveState.SAVING ||
            synchronizationState.value is SynchronizationState.InProgress
        ) return
        favoriteSaveState.value = FavoriteSaveState.SAVING
        viewModelScope.launch {
            try {
                favoriteApi.updateFavorites(paths.distinct())
                favoriteSaveState.value = FavoriteSaveState.IDLE
                onSaved()
                synchronizationApi.startSynchronization(force = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (_: Exception) {
                favoriteSaveState.value = FavoriteSaveState.ERROR
            }
        }
    }
}
