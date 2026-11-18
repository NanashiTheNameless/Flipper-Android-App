package dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.api.FScreenStreamingFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.screen.InputKey
import com.flipperdevices.protobuf.screen.InputType
import com.flipperdevices.protobuf.screen.ScreenFrame
import com.flipperdevices.protobuf.screen.SendAsciiEventRequest
import com.flipperdevices.protobuf.screen.SendInputEventRequest
import com.flipperdevices.protobuf.screen.StartScreenStreamRequest
import com.flipperdevices.protobuf.screen.StopScreenStreamRequest
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.mapNotNull

// The firmware accepts any value up to 0xFF
private const val MAX_ASCII_EVENT_VALUE = 0xFF

class FScreenStreamingFeatureApiImpl @AssistedInject constructor(
    @Assisted private val rpcFeatureApi: FRpcFeatureApi,
) : FScreenStreamingFeatureApi,
    LogTagProvider {
    override val TAG = "FScreenStreamingFeatureApi"

    override suspend fun sendInputAndForget(inputKey: InputKey, inputType: InputType) {
        return rpcFeatureApi.requestWithoutAnswer(
            Main(
                gui_send_input_event_request = SendInputEventRequest(
                    key = inputKey,
                    type = inputType
                )
            ).wrapToRequest()
        )
    }

    override suspend fun sendAscii(text: String): Result<Unit> = runCatching {
        for (char in text) {
            require(char.code <= MAX_ASCII_EVENT_VALUE) { "Can't type '$char' on the Flipper" }
            rpcFeatureApi.requestOnce(
                Main(
                    gui_send_ascii_event_request = SendAsciiEventRequest(value_ = char.code)
                ).wrapToRequest()
            ).getOrThrow()
        }
    }

    override suspend fun awaitInput(inputKey: InputKey, inputType: InputType): Flow<Result<Main>> {
        return rpcFeatureApi.request(
            Main(
                gui_send_input_event_request = SendInputEventRequest(
                    key = inputKey,
                    type = inputType
                )
            ).wrapToRequest()
        )
    }

    override suspend fun stop(): Result<Main> {
        return rpcFeatureApi.requestOnce(
            Main(
                gui_stop_screen_stream_request = StopScreenStreamRequest()
            ).wrapToRequest()
        )
    }

    override suspend fun guiScreenFrameFlow(): Flow<ScreenFrame> {
        return rpcFeatureApi.request(
            Main(
                gui_start_screen_stream_request = StartScreenStreamRequest()
            ).wrapToRequest()
        ).mapNotNull { result -> result.getOrNull()?.gui_screen_frame }
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            rpcFeatureApi: FRpcFeatureApi,
        ): FScreenStreamingFeatureApiImpl
    }
}
