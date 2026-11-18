package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.AndroidKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.DiffKeyExecutorImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor.FlipperKeyStorage
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronizationImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FolderKeySynchronizationImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.KeyDiffApplierImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.KeysSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.KeysSynchronizationImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.android.AndroidHashRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.android.SynchronizationStateRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperFavoritesRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.FlipperHashRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper.TimestampSynchronizationCheckerImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.DiffMergeHelperImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepositoryImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestStorageImpl
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestTimestampRepositoryImpl

class TaskSynchronizationComponentImpl(
    deps: TaskSynchronizationComponentDependencies,
    storageFeatureApi: FStorageFeatureApi
) : TaskSynchronizationComponent,
    TaskSynchronizationComponentDependencies by deps {
    private val manifestStorage = ManifestStorageImpl(context)

    override val manifestRepository = ManifestRepositoryImpl(
        manifestStorage = manifestStorage
    )

    private val androidHashRepositoryImpl = AndroidHashRepositoryImpl()

    private val flipperHashRepository = FlipperHashRepositoryImpl(
        flipperStorageApi = storageFeatureApi.listingApi()
    )

    private val synchronizationRepository = SynchronizationStateRepositoryImpl(
        utilsKeyApi = utilsKeyApi
    )

    private val diffMergeHelper = DiffMergeHelperImpl(
        simpleKeyApi = simpleKeyApi,
        utilsKeyApi = utilsKeyApi,
        updateKeyApi = updateKeyApi
    )

    private val androidStorage = AndroidKeyStorage(
        simpleKeyApi = simpleKeyApi,
        deleteKeyApi = deleteKeyApi,
        flipperFileApi = flipperFileApi
    )

    private val flipperStorage = FlipperKeyStorage(
        fileDownloadApi = storageFeatureApi.downloadApi(),
        fileUploadApi = storageFeatureApi.uploadApi(),
        fileDeleteApi = storageFeatureApi.deleteApi()
    )

    private val diffKeyExecutor = DiffKeyExecutorImpl()

    private val keyDiffApplier = KeyDiffApplierImpl(
        diffMergeHelper = diffMergeHelper,
        diffKeyExecutor = diffKeyExecutor,
        androidStorage = androidStorage,
        flipperStorage = flipperStorage
    )

    private val folderKeySynchronization = FolderKeySynchronizationImpl(
        androidHashRepository = androidHashRepositoryImpl,
        flipperHashRepository = flipperHashRepository,
        manifestRepository = manifestRepository,
        synchronizationRepository = synchronizationRepository,
        simpleKeyApi = simpleKeyApi,
        keyDiffApplier = keyDiffApplier,
    )

    private val timestampSynchronizationChecker = TimestampSynchronizationCheckerImpl(
        timestampApi = storageFeatureApi.timestampApi()
    )

    private val manifestTimestampRepository = ManifestTimestampRepositoryImpl(
        manifestStorage = manifestStorage
    )

    private val favoritesRepository = FlipperFavoritesRepositoryImpl()

    override val keysSynchronization: KeysSynchronization = KeysSynchronizationImpl(
        folderKeySynchronization,
        timestampSynchronizationChecker,
        manifestTimestampRepository,
        manifestRepository,
        AndroidHashRepositoryImpl(),
        simpleKeyApi
    )

    override val favoriteSynchronization: FavoriteSynchronization = FavoriteSynchronizationImpl(
        favoriteApi = favoriteApi,
        manifestRepository = manifestRepository,
        flipperStorage = flipperStorage,
        favoritesRepository = favoritesRepository
    )
}
