package dev.namelessnanashi.flipperzero.updater.card.helpers

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.updater.card.helpers.delegates.UpdateOfferDelegate
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface UpdateOfferProviderApi {
    fun isUpdateRequire(fStorageFeatureApi: FStorageFeatureApi): Flow<Boolean>
}

@ContributesBinding(AppGraph::class, binding<UpdateOfferProviderApi>())
class UpdateOfferProvider @Inject constructor(
    private val delegates: Set<UpdateOfferDelegate>
) : UpdateOfferProviderApi {

    override fun isUpdateRequire(fStorageFeatureApi: FStorageFeatureApi): Flow<Boolean> {
        return combine(
            flows = delegates.map { it.isRequire(fStorageFeatureApi) },
            transform = { delegate -> return@combine delegate.any { it } }
        )
    }
}
