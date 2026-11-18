package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di

import android.content.Context
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FavoriteApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.FlipperFileApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.DeleteKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UpdateKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UtilsKeyApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.FavoriteSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.KeysSynchronization
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.manifest.ManifestRepository
import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppGraph::class)
interface TaskSynchronizationComponentDependencies {
    val simpleKeyApi: SimpleKeyApi
    val deleteKeyApi: DeleteKeyApi
    val utilsKeyApi: UtilsKeyApi
    val favoriteApi: FavoriteApi
    val flipperFileApi: FlipperFileApi
    val updateKeyApi: UpdateKeyApi
    val context: Context
    val settings: DataStore<Settings>
    val flipperStorageApi: FlipperStorageProvider
}

interface TaskSynchronizationComponent {
    val keysSynchronization: KeysSynchronization
    val favoriteSynchronization: FavoriteSynchronization
    val manifestRepository: ManifestRepository

    /**
     * This [ManualFactory] is required to escape from usage of kapt inside this module.
     *
     * [ManualFactory.create] will return manually created [TaskSynchronizationComponent] instance
     */
    object ManualFactory {
        fun create(
            deps: TaskSynchronizationComponentDependencies,
            storageFeatureApi: FStorageFeatureApi
        ): TaskSynchronizationComponent = TaskSynchronizationComponentImpl(
            deps = deps,
            storageFeatureApi = storageFeatureApi
        )
    }
}
