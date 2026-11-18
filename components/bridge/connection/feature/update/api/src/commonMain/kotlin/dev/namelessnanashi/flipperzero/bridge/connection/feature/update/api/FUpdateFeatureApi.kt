package dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi

interface FUpdateFeatureApi : FDeviceFeatureApi {
    fun displayApi(): DisplayApi
    fun bootApi(): BootApi

    companion object {
        const val MANIFEST_FILE = "/ext/Manifest"
    }
}
