package dev.namelessnanashi.flipperzero.bridge.connection.feature.update.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.BootApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.DisplayApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.FUpdateFeatureApi
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class FUpdateFeatureApiImpl @AssistedInject constructor(
    @Assisted private val displayApi: DisplayApi,
    @Assisted private val bootApi: BootApi,
) : FUpdateFeatureApi,
    LogTagProvider {
    override val TAG = "FAlarmFeatureApi"

    override fun bootApi(): BootApi = bootApi

    override fun displayApi(): DisplayApi = displayApi

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            displayApi: DisplayApi,
            bootApi: BootApi,
        ): FUpdateFeatureApiImpl
    }
}
