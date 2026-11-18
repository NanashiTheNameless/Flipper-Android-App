package dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.screen.InputKey
import com.flipperdevices.protobuf.screen.InputType
import com.flipperdevices.protobuf.screen.ScreenFrame
import kotlinx.coroutines.flow.Flow

interface FScreenStreamingFeatureApi : FDeviceFeatureApi {
    suspend fun awaitInput(inputKey: InputKey, inputType: InputType): Flow<Result<Main>>

    suspend fun sendInputAndForget(inputKey: InputKey, inputType: InputType)

    /**
     * Types [text] on the Flipper through Momentum's ASCII events (`SendAsciiEventRequest`); text
     * input screens receive the characters. Official firmware does not support this and fails.
     */
    suspend fun sendAscii(text: String): Result<Unit>

    suspend fun stop(): Result<Main>

    /**
     * Start collect [ScreenFrame].
     * Need to call [stop] to cancel data sending from flipper
     */
    suspend fun guiScreenFrameFlow(): Flow<ScreenFrame>
}
