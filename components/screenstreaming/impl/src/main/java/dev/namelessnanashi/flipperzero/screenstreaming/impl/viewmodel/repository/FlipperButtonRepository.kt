package dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.screenstreaming.api.FScreenStreamingFeatureApi
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import com.flipperdevices.protobuf.screen.InputKey
import com.flipperdevices.protobuf.screen.InputType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject

class FlipperButtonRepository @Inject constructor(
    private val fFeatureProvider: FFeatureProvider
) : LogTagProvider {
    override val TAG: String = "FlipperButtonRequest"

    private val mutex = Mutex()

    fun pressOnButton(
        viewModelScope: CoroutineScope,
        key: InputKey,
        type: InputType,
        onComplete: () -> Unit = {}
    ) = launchWithLock(mutex = mutex, scope = viewModelScope) {
        val fScreenStreamingFeatureApi = fFeatureProvider.getSync<FScreenStreamingFeatureApi>()
        if (fScreenStreamingFeatureApi == null) {
            error { "#pressOnButton FScreenStreamingFeatureApi not found!" }
            return@launchWithLock
        }

        fScreenStreamingFeatureApi.sendInputAndForget(key, InputType.PRESS)
        fScreenStreamingFeatureApi.sendInputAndForget(key, type)

        fScreenStreamingFeatureApi.awaitInput(key, InputType.RELEASE)
            .onEach { result ->
                result.onFailure { error(it) { "#pressOnButton InputType.RELEASE failed" } }
            }
            .onEach { onComplete.invoke() }
            .collect()
    }
}
