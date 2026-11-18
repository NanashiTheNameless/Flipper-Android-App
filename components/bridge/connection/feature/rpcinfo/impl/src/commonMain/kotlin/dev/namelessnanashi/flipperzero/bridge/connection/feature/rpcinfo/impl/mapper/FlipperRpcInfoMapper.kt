package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.mapper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation

internal interface FlipperRpcInfoMapper<KEY> {
    fun map(raw: InternalFlipperRpcInformationRaw<KEY>): FlipperRpcInformation
}
