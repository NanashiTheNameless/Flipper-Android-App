package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm.delete

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDeleteApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.model.StorageRequestPriority
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.utils.toRpc
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.DeleteRequest

class FFileDeleteApiImpl(
    private val rpcApi: FRpcFeatureApi
) : FFileDeleteApi {
    override suspend fun delete(
        path: String,
        recursive: Boolean,
        priority: StorageRequestPriority
    ): Result<Unit> {
        return rpcApi.requestOnce(
            Main(
                storage_delete_request = DeleteRequest(
                    path = path,
                    recursive = recursive
                )
            ).wrapToRequest(priority.toRpc())
        ).map { }
    }
}
