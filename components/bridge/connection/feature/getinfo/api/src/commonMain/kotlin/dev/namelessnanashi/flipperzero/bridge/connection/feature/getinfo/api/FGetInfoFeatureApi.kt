package dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiGroup
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty
import kotlinx.coroutines.flow.Flow

interface FGetInfoFeatureApi : FDeviceFeatureApi {
    suspend fun get(property: FGetInfoApiProperty): Result<String>

    fun get(group: FGetInfoApiGroup): Flow<Pair<FGetInfoApiProperty, String>>
}
