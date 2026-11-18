package dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.api.FStorageInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.FlipperStorageInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.StorageStats
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FlipperNotConnected
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.error.NoSdCardException
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.model.FapManifestLoaderState
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.impl.utils.FapManifestConstants.FAP_MANIFESTS_FOLDER_ON_FLIPPER
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import java.io.File
import kotlinx.collections.immutable.persistentListOf
import kotlinx.collections.immutable.toPersistentList
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChangedBy
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit
import okio.buffer

private const val PARALLEL_MANIFEST_LOADS = 4

@Suppress("LongParameterList")
class FapManifestsLoader @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    private val parser: FapManifestParser,
    private val dataStoreSettings: DataStore<Settings>,
    private val cacheLoader: FapManifestCacheLoader,
    private val fapExistChecker: FapExistChecker,
    private val fFeatureProvider: FFeatureProvider,
    private val fDeviceOrchestrator: FDeviceOrchestrator
) : LogTagProvider {
    override val TAG = "FapManifestsLoader"
    private val manifestLoaderState = MutableStateFlow<FapManifestLoaderState>(
        FapManifestLoaderState.Loaded(
            items = persistentListOf(),
            isLoading = true
        )
    )

    private var job: Job? = null
    private val jobInvalidateMutex = Mutex()

    fun invalidate() = launchWithLock(jobInvalidateMutex, scope) {
        val oldJob = job
        job = scope.launch {
            oldJob?.cancelAndJoin()
            fapExistChecker.invalidate()
            manifestLoaderState.emit(
                FapManifestLoaderState.Loaded(
                    items = persistentListOf(),
                    isLoading = true
                )
            )
            // Feature APIs belong to one connection: every reconnect (background keep-alive,
            // BLE -> USB switch) makes a new one. Follow the current one, or the loader keeps
            // watching a dead instance whose storage info never loads and reports "no SD card".
            val storageInformationFlow = fFeatureProvider.get<FStorageInfoFeatureApi>()
                .flatMapLatest { status ->
                    when (status) {
                        is FFeatureStatus.Supported -> {
                            status.featureApi.invalidate(scope = scope)
                            status.featureApi.getStorageInformationFlow()
                        }

                        FFeatureStatus.Retrieving -> flowOf(FlipperStorageInformation())
                        FFeatureStatus.NotFound,
                        FFeatureStatus.Unsupported -> {
                            error { "#invalidate could not find FStorageInfoFeatureApi" }
                            flowOf(
                                FlipperStorageInformation(
                                    externalStorageStatus = FlipperInformationStatus.Ready(null)
                                )
                            )
                        }
                    }
                }

            combine(
                fDeviceOrchestrator.getState(),
                storageInformationFlow
            ) { connectionState, flipperStorageInformation ->
                connectionState to flipperStorageInformation
            }.distinctUntilChangedBy { (connectionState, flipperStorageInformation) ->
                // Storage info arrives in several updates after connect; restarting on each one
                // abandons the md5 listing of the manifests folder, which the Flipper still streams in full
                val externalStorage = flipperStorageInformation.externalStorageStatus
                    as? FlipperInformationStatus.Ready<StorageStats?>
                Triple(
                    connectionState is FDeviceConnectStatus.Connected,
                    externalStorage != null,
                    externalStorage?.data is StorageStats.Loaded
                )
            }.collectLatest { (connectionState, flipperStorageInformation) ->
                runCatching {
                    loadInternal(
                        connectionState = connectionState,
                        storageInformation = flipperStorageInformation
                    )
                }.onFailure {
                    if (it is CancellationException) {
                        throw it
                    } else {
                        manifestLoaderState.emit(FapManifestLoaderState.Failed(it))
                    }
                }
            }
        }
    }

    fun getManifestLoaderState() = manifestLoaderState.asStateFlow()

    @Suppress("LongMethod")
    private suspend fun loadInternal(
        connectionState: FDeviceConnectStatus,
        storageInformation: FlipperStorageInformation
    ) {
        val isUseDevCatalog = dataStoreSettings.data.first().use_dev_catalog

        if (connectionState !is FDeviceConnectStatus.Connected) {
            throw FlipperNotConnected()
        }
        val externalStorageStatus = storageInformation.externalStorageStatus
            as? FlipperInformationStatus.Ready<StorageStats?>
        if (externalStorageStatus == null) {
            // Storage info still loading (fresh connection): not an answer yet, keep waiting
            manifestLoaderState.emit(
                FapManifestLoaderState.Loaded(
                    items = persistentListOf(),
                    isLoading = true
                )
            )
            return
        }
        if (externalStorageStatus.data !is StorageStats.Loaded) {
            throw NoSdCardException()
        }
        manifestLoaderState.emit(
            FapManifestLoaderState.Loaded(
                items = persistentListOf(),
                isLoading = true
            )
        )
        info { "Start load manifests" }
        val cacheResult = cacheLoader.loadCache()
        info { "Cache load result is toLoad: ${cacheResult.toLoadNames}, cached: ${cacheResult.cachedNames}" }
        val fapItemsList = mutableListOf<FapManifestItem>()
        val cachedManifests = cacheResult.cachedNames.mapNotNull { (file, name) ->
            parser.parse(file.readBytes(), name)
        }
        // List every category folder at once instead of one per first check
        fapExistChecker.prefetch(cachedManifests.map { it.path })
        cachedManifests.filter { fapExistChecker.checkExist(it.path) }
            .filter { it.isDevCatalog == isUseDevCatalog }
            .forEach {
                fapItemsList.add(it)
                manifestLoaderState.emit(
                    FapManifestLoaderState.Loaded(
                        items = fapItemsList.toPersistentList(),
                        isLoading = true
                    )
                )
            }
        info { "Parsed ${fapItemsList.size} manifests from cache" }
        // Each manifest read is a full round trip to the Flipper; keep a few in flight
        // instead of leaving the link idle between files. Order and gradual updates stay the same.
        coroutineScope {
            val loadPermits = Semaphore(PARALLEL_MANIFEST_LOADS)
            cacheResult.toLoadNames.map { name ->
                async(FlipperDispatchers.workStealingDispatcher) {
                    val content = loadPermits.withPermit {
                        loadManifestFile(filePath = File(FAP_MANIFESTS_FOLDER_ON_FLIPPER, name).absolutePath)
                            ?.let { byteArray -> parser.parse(byteArray, name) }
                    }
                    // Checked here, in parallel, rather than one by one in the ordered loop below
                    content?.takeIf {
                        it.isDevCatalog == isUseDevCatalog && fapExistChecker.checkExist(it.path)
                    }
                }
            }.forEach { loading ->
                val content = loading.await() ?: return@forEach
                fapItemsList.add(content)
                manifestLoaderState.emit(
                    FapManifestLoaderState.Loaded(
                        items = fapItemsList.toPersistentList(),
                        isLoading = true
                    )
                )
            }
        }
        info { "Parsed ${fapItemsList.size} manifests from flipper" }

        cacheLoader.invalidate(fapItemsList)

        manifestLoaderState.emit(
            FapManifestLoaderState.Loaded(
                items = fapItemsList.toPersistentList(),
                isLoading = false
            )
        )
    }

    private suspend fun loadManifestFile(
        filePath: String
    ): ByteArray? = coroutineScope {
        fFeatureProvider
            .getSync<FStorageFeatureApi>()
            ?.downloadApi()
            ?.source(
                pathOnFlipper = filePath,
                priority = StorageRequestPriority.BACKGROUND,
                scope = this
            )
            ?.buffer()
            ?.readByteArray()
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(scope: CoroutineScope): FapManifestsLoader
    }
}
