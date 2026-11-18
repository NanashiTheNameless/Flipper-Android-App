package dev.namelessnanashi.flipperzero.updater.card.helpers.delegates

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.zacsweers.metro.ContributesIntoSet
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesIntoSet(AppGraph::class, binding<UpdateOfferDelegate>())
class UpdateOfferDebugFlagAlways @Inject constructor(
    private val dataStoreSettings: DataStore<Settings>
) : UpdateOfferDelegate {

    override fun isRequire(fStorageFeatureApi: FStorageFeatureApi): Flow<Boolean> {
        return dataStoreSettings.data.map { it.always_update }
    }
}
