package dev.namelessnanashi.flipperzero.updater.card.helpers.delegates

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import kotlinx.coroutines.flow.Flow

interface UpdateOfferDelegate {
    fun isRequire(fStorageFeatureApi: FStorageFeatureApi): Flow<Boolean>
}
