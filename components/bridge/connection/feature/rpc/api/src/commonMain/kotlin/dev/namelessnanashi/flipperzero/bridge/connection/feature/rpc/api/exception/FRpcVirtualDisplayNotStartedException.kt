package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.exception

import com.flipperdevices.protobuf.Main

data class FRpcVirtualDisplayNotStartedException(
    override val response: Main
) : FRpcException()
