package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapBatchUpdateButtonState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapInstalledInternalState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapInstalledScreenState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.InstalledFapApp
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.InstalledNetworkErrorEnum
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.toButtonState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.toScreenState
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

class InstalledFapsViewModel @Inject constructor(
    private val fapManifestApi: FapManifestApi,
    private val queueApi: FapInstallationQueueApi,
    fapsStateProducerProvider: Provider<InstalledFapsFromNetworkProducer>,
    private val favoriteApi: FavoriteApi,
    private val synchronizationApi: SynchronizationApi
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "InstalledFapsViewModel"
    private var fetcherJob: Job? = null
    val favoritePaths = MutableStateFlow<Set<FlipperKeyPath>>(emptySet())
    val favoriteChanges = MutableStateFlow<Set<String>>(emptySet())
    val favoriteError = MutableStateFlow(false)
    val synchronizationState = synchronizationApi.getSynchronizationState()

    private val fapsStateProducer by fapsStateProducerProvider
    private val installedFapsStateFlow = MutableStateFlow<FapInstalledInternalLoadingState>(
        FapInstalledInternalLoadingState.Loaded(
            faps = persistentListOf(),
            inProgress = true
        )
    )
    private val screenStateFlow = MutableStateFlow<FapInstalledScreenState>(
        FapInstalledScreenState.Loaded(
            faps = persistentListOf(),
            inProgress = true
        )
    )
    private val batchUpdateButtonState = MutableStateFlow<FapBatchUpdateButtonState>(
        FapBatchUpdateButtonState.Loading
    )

    init {
        favoriteApi.getFavoritePathsFlow().onEach {
            favoritePaths.value = it.toSet()
        }.launchIn(viewModelScope)
        refresh(force = false)
        installedFapsStateFlow.onEach {
            screenStateFlow.emit(it.toScreenState())
        }.launchIn(viewModelScope)

        installedFapsStateFlow.onEach {
            batchUpdateButtonState.emit(it.toButtonState())
        }.launchIn(viewModelScope)
    }

    internal fun getFapInstalledScreenState() = screenStateFlow.asStateFlow()

    fun getFapBatchUpdateButtonState() = batchUpdateButtonState.asStateFlow()

    fun getAppFavoritePath(applicationUid: String): FlipperKeyPath? =
        (fapManifestApi.getManifestFlow().value as? FapManifestState.Loaded)
            ?.items?.firstOrNull { it.uid == applicationUid }
            ?.let { FlipperKeyPath(FlipperFilePath.fromFavoritePath(it.path), false) }

    fun toggleFavorite(applicationUid: String) {
        if (applicationUid in favoriteChanges.value || synchronizationState.value is SynchronizationState.InProgress) {
            return
        }
        val path = getAppFavoritePath(applicationUid) ?: return
        favoriteChanges.update { it + applicationUid }
        favoriteError.value = false
        viewModelScope.launch {
            try {
                favoriteApi.setFavorite(path, !favoriteApi.isFavorite(path))
                synchronizationApi.startSynchronization(force = true)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (exception: Exception) {
                error(exception) { "Could not change app favorite" }
                favoriteError.value = true
            } finally {
                favoriteChanges.update { it - applicationUid }
            }
        }
    }

    fun updateAll() = viewModelScope.launch {
        val state = installedFapsStateFlow.first()
        if (state !is FapInstalledInternalLoadingState.Loaded) {
            info { "State is $state, so just return" }
            return@launch
        }
        val toUpdate = state.faps.filter { (fapApp, state) ->
            state is FapInstalledInternalState.ReadyToUpdate &&
                fapApp is InstalledFapApp.OnlineFapApp
        }
        info { "Pending items is $toUpdate" }

        toUpdate.forEach { (fapItem, state) ->
            if (state is FapInstalledInternalState.ReadyToUpdate &&
                fapItem is InstalledFapApp.OnlineFapApp
            ) {
                queueApi.enqueueSync(
                    FapActionRequest.Update(
                        from = state.manifestItem,
                        toVersion = fapItem.fapItemShort.upToDateVersion,
                        iconUrl = fapItem.fapItemShort.picUrl,
                        applicationName = fapItem.name
                    )
                )
            }
        }
    }

    fun cancelAll() = viewModelScope.launch {
        val state = installedFapsStateFlow.first()
        if (state !is FapInstalledInternalLoadingState.Loaded) {
            info { "State is $state, so just return" }
            return@launch
        }
        state.faps.filter { (_, state) -> state is FapInstalledInternalState.UpdatingInProgress }
            .forEach { (fapItem, _) ->
                queueApi.enqueueSync(FapActionRequest.Cancel(fapItem.applicationUid))
            }
    }

    fun refresh(force: Boolean) {
        fapsStateProducer.refresh(force)
        val oldJob = fetcherJob
        fetcherJob = viewModelScope.launch(FlipperDispatchers.workStealingDispatcher) {
            oldJob?.cancelAndJoin()
            installedFapsStateFlow.emit(
                FapInstalledInternalLoadingState.Loaded(
                    faps = persistentListOf(),
                    inProgress = true
                )
            )
            if (force) {
                fapManifestApi.invalidateAsync()
            }

            fapsStateProducer.getLoadedFapsFlow().catch {
                if (it is CancellationException) {
                    return@catch
                }
                error(it) { "Failed get installed items" }
                installedFapsStateFlow.emit(FapInstalledInternalLoadingState.Error(it))
            }.collectLatest {
                installedFapsStateFlow.emit(it)
            }
        }
    }
}

sealed class FapInstalledInternalLoadingState {
    data class Loaded(
        val faps: ImmutableList<Pair<InstalledFapApp, FapInstalledInternalState>>,
        val inProgress: Boolean,
        val networkError: InstalledNetworkErrorEnum? = null
    ) : FapInstalledInternalLoadingState()

    data class Error(
        val throwable: Throwable
    ) : FapInstalledInternalLoadingState()
}
