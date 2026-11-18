package dev.namelessnanashi.flipperzero.bridge.connection.device.common.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import kotlin.reflect.KClass

interface FDeviceApi {
    suspend fun <T : FDeviceFeatureApi> get(clazz: KClass<T>): T?
}

suspend inline fun <reified T : FDeviceFeatureApi> FDeviceApi.get(): T? {
    return get(T::class)
}
