package dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import com.flipperdevices.protobuf.Main

interface FScreenUnlockFeatureApi : FDeviceFeatureApi {
    suspend fun unlock(): Result<Main>
}
