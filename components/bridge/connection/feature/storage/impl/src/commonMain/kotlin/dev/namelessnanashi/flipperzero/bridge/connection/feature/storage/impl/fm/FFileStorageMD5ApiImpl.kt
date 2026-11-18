package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl.fm

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileStorageMD5Api
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.storage.Md5sumRequest

class FFileStorageMD5ApiImpl(
    private val rpcApi: FRpcFeatureApi
) : FFileStorageMD5Api {
    override suspend fun md5(path: String): Result<String> = runCatching {
        val response = rpcApi.requestOnce(
            Main(
                storage_md5sum_request = Md5sumRequest(path = path)
            ).wrapToRequest()
        ).getOrThrow()

        val md5 = response.storage_md5sum_response?.md5sum ?: error("MD5 for file $path is null")

        if (md5.isBlank()) {
            error("MD5 for file $path is blank")
        }
        return@runCatching md5
    }
}
