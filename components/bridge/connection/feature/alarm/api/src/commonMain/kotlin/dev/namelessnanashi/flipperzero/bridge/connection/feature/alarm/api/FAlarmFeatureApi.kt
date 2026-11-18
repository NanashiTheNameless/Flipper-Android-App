package dev.namelessnanashi.flipperzero.bridge.connection.feature.alarm.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi

interface FAlarmFeatureApi : FDeviceFeatureApi {
    suspend fun makeSound()
}
