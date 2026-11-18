package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import com.flipperdevices.protobuf.Main
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

interface FRpcFeatureApi : FDeviceFeatureApi {
    /**
     * You can subscribe on this flow if you want receive message without request
     */
    fun notificationFlow(): Flow<Main>

    /**
     * Emits when answers arrive for commands this app never sent, which means another app
     * (for example the official Flipper app) shares the connection. Both writing to storage at
     * once corrupts files.
     */
    fun otherClientDetected(): Flow<Unit> = emptyFlow()

    /**
     * Send request and wait answer from them.
     *
     * You can use the extension Flow<Result<T>>.toThrowableFlow(): Flow<T> for more convenient error catching
     */
    fun request(command: FlipperRequest): Flow<Result<Main>>

    /**
     * Send request and wait single answer
     */
    suspend fun requestOnce(command: FlipperRequest): Result<Main>

    /**
     * Send batch of request in flipper and wait single answer
     */
    suspend fun request(
        commandFlow: Flow<FlipperRequest>,
        onCancel: suspend (Int) -> Unit = {}
    ): Result<Main>

    /**
     * Send batch request without waiting response
     */
    suspend fun requestWithoutAnswer(vararg commands: FlipperRequest)
}
