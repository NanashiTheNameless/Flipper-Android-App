package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.fullinforpc

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiGroup
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.mapper.NewFlipperRpcInfoMapper
import kotlinx.coroutines.flow.merge

internal class NewFlipperFullInfoRpcApi(
    private val getInfoFeature: FGetInfoFeatureApi
) : FlipperFullInfoRpcApi<FGetInfoApiProperty>(
    NewFlipperRpcInfoMapper()
) {
    override suspend fun getRawDataFlow(
        requestApi: FRpcFeatureApi,
        onNewPair: suspend (key: FGetInfoApiProperty, value: String) -> Unit
    ) {
        FGetInfoApiGroup.entries.map { infoGroup ->
            getInfoFeature.get(infoGroup)
        }.merge().collect { (property, value) ->
            onNewPair(
                property,
                value
            )
        }
    }
}
