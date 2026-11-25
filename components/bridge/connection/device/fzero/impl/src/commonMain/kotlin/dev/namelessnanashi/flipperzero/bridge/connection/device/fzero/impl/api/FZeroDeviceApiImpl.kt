package dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.api.FZeroDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.device.fzero.impl.utils.FZeroFeatureClassToEnumMapper
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeature
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FOnDeviceReadyFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FUnsafeDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.FConnectedDeviceApi
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import java.util.EnumMap
import kotlin.reflect.KClass

@ContributesAssistedFactory(AppGraph::class, FZeroDeviceApi.Factory::class)
class FZeroDeviceApiImpl @AssistedInject constructor(
    @Assisted private val scope: CoroutineScope,
    @Assisted private val connectedDevice: FConnectedDeviceApi,
    onReadyFeaturesApiFactories: Set<FOnDeviceReadyFeatureApi.Factory>,
    private val factories: Map<FDeviceFeature, FDeviceFeatureApi.Factory>
) : FZeroDeviceApi, FUnsafeDeviceFeatureApi, LogTagProvider {
    override val TAG = "FZeroDeviceApi"

    private val features = EnumMap<FDeviceFeature, FDeviceFeatureApi>(FDeviceFeature::class.java)
    private val mutex = Mutex()

    init {
        if (BuildKonfig.CRASH_APP_ON_FAILED_CHECKS) {
            FDeviceFeature.entries.forEach { key ->
                checkNotNull(factories[key]) { "Not found factory for $key" }
            }
        }

        scope.launch {
            callAllOnReadyDeviceFeatures(onReadyFeaturesApiFactories)
        }
    }

    override suspend fun <T : FDeviceFeatureApi> get(clazz: KClass<T>): T? = mutex.withLock {
        return@withLock getUnsafe(clazz)
    }

    override suspend fun <T : FDeviceFeatureApi> getUnsafe(clazz: KClass<T>): T? {
        val deviceFeature = FZeroFeatureClassToEnumMapper.get(clazz) ?: return null
        val featureApi = getFeatureApi(deviceFeature)
        if (!clazz.isInstance(featureApi)) {
            return null
        }
        @Suppress("UNCHECKED_CAST")
        return featureApi as? T
    }

    private suspend fun getFeatureApi(feature: FDeviceFeature): FDeviceFeatureApi? {
        var featureApi = features[feature]
        if (featureApi != null) {
            return featureApi
        }
        val factory = factories[feature]
        if (factory == null) {
            error { "Fail to find factory for feature $feature" }
            return null
        }
        info { "$feature feature start creation..." }
        featureApi = factory(
            unsafeFeatureDeviceApi = this,
            scope = scope,
            connectedDevice = connectedDevice
        )
        if (featureApi == null) {
            error { "Fail to create $feature!" }
        } else {
            info { "$feature feature creation successful!" }
        }
        features[feature] = featureApi
        return featureApi
    }

    private suspend fun callAllOnReadyDeviceFeatures(
        factories: Set<FOnDeviceReadyFeatureApi.Factory>
    ) = mutex.withLock {
        for (factory in factories) {
            try {
                val featureApi = factory(
                    unsafeFeatureDeviceApi = this,
                    scope = scope,
                    connectedDevice = connectedDevice
                )
                featureApi?.onReady()
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (e: Throwable) {
                error(e) { "Failed init on ready device factory $factory" }
            }
        }
    }
}
