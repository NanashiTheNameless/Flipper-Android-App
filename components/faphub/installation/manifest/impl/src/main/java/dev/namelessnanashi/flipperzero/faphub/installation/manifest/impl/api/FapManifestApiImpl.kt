package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestDeleter
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestUploader
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestsLoader
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.zacsweers.metro.ContributesBinding
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FapManifestApi>())
class FapManifestApiImpl @Inject constructor(
    loaderFactory: FapManifestsLoader.Factory,
    private val manifestUploader: FapManifestUploader,
    private val manifestDeleter: FapManifestDeleter,
) : FapManifestApi, LogTagProvider {
    override val TAG = "FapManifestApi"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val loader = loaderFactory(scope)

    private val fapManifestStateFlow = MutableStateFlow<FapManifestState>(
        FapManifestState.Loaded(
            items = persistentListOf(),
            inProgress = true
        )
    )

    private val mutex = Mutex()

    private var job: Job? = null
    private val jobInvalidateMutex = Mutex()

    /**
     * Installs, updates and deletes made by this app, by application uid (null item = deleted).
     * A reload that was already reading manifests when one of them finished emits the old .fim
     * contents and used to overwrite it, so an updated app showed "Update" again. They are
     * applied on top of every reload until a reload starts after them (it then reads them from
     * the Flipper), and dropped when the Flipper disconnects. Guarded by [mutex].
     */
    private val localChanges = mutableMapOf<String, LocalChange>()
    private var loadGeneration = 0L

    init {
        invalidateAsync()
    }

    override fun getManifestFlow() = fapManifestStateFlow.asStateFlow()

    override suspend fun add(
        pathToFap: String,
        fapManifestItem: FapManifestItem
    ) = withLock(mutex, "add") {
        info { "Add $pathToFap to $fapManifestItem " }

        manifestUploader.save(pathToFap, fapManifestItem)
        localChanges[fapManifestItem.uid] = LocalChange(fapManifestItem, loadGeneration)
        fapManifestStateFlow.update { fapManifestState ->
            if (fapManifestState is FapManifestState.Loaded) {
                fapManifestState.copy(
                    items = fapManifestState
                        .items
                        .filter { it.uid != fapManifestItem.uid }
                        .plus(fapManifestItem)
                        .toPersistentList()
                )
            } else {
                fapManifestState
            }
        }
    }

    override suspend fun remove(
        applicationUid: String
    ) = withLock(mutex, "remove") {
        val toRemoveManifests = (fapManifestStateFlow.value as? FapManifestState.Loaded)
            ?.items
            ?.filter { it.uid == applicationUid }
        info { "On $applicationUid toRemoveManifests is $toRemoveManifests" }

        if (toRemoveManifests.isNullOrEmpty()) {
            return@withLock
        }
        localChanges[applicationUid] = LocalChange(item = null, generation = loadGeneration)

        fapManifestStateFlow.update { fapManifestState ->
            if (fapManifestState is FapManifestState.Loaded) {
                fapManifestState.copy(
                    items = fapManifestState
                        .items
                        .minus(toRemoveManifests)
                        .toPersistentList()
                )
            } else {
                fapManifestState
            }
        }
        toRemoveManifests.forEach {
            manifestDeleter.delete(it)
        }
    }

    override fun invalidateAsync() = launchWithLock(jobInvalidateMutex, scope) {
        val oldJob = job
        job = scope.launch {
            oldJob?.cancelAndJoin()
            withLock(mutex, "new_generation") {
                // Changes finished before this reload starts are on the Flipper: it reads them
                loadGeneration++
                localChanges.values.removeAll { it.generation < loadGeneration }
            }
            loader.invalidate()
            loader.getManifestLoaderState()
                .collectLatest { manifestLoaderState ->
                    withLock(mutex, "invalidate") {
                        val state = manifestLoaderState.toManifestState()
                        if (state is FapManifestState.NotLoaded) {
                            // Disconnected: the next load reads everything from the Flipper again
                            localChanges.clear()
                        }
                        fapManifestStateFlow.emit(state.withLocalChanges())
                    }
                }
        }
    }

    private fun FapManifestState.withLocalChanges(): FapManifestState {
        if (this !is FapManifestState.Loaded || localChanges.isEmpty()) {
            return this
        }
        return copy(
            items = items
                .filter { it.uid !in localChanges }
                .plus(localChanges.values.mapNotNull { it.item })
                .toPersistentList()
        )
    }

    private class LocalChange(val item: FapManifestItem?, val generation: Long)
}
