package dev.namelessnanashi.flipperzero.widget.impl.storage

import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.FEmulateFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.WidgetDataApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLockResult
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetState
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

interface WidgetStateStorage {
    suspend fun updateState(widgetId: Int, state: WidgetState)
    suspend fun getState(widgetId: Int): WidgetState
}

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<WidgetStateStorage>())
class WidgetStateStorageImpl @Inject constructor(
    private val fFeatureProvider: FFeatureProvider,
    private val widgetDataApi: WidgetDataApi
) : WidgetStateStorage, LogTagProvider {
    override val TAG = "WidgetStateStorage"

    private val stateMap = HashMap<Int, WidgetState>()
    private val mutex = Mutex()

    override suspend fun updateState(
        widgetId: Int,
        state: WidgetState
    ) = withLock(mutex, "update") {
        info { "Update state for $widgetId to $state" }
        if (state == WidgetState.IN_PROGRESS) {
            stateMap.forEach {
                if (it.value == WidgetState.IN_PROGRESS) {
                    stateMap[it.key] = WidgetState.PENDING
                }
            }
        }
        stateMap[widgetId] = state
    }

    override suspend fun getState(
        widgetId: Int
    ): WidgetState = withLockResult(mutex, "get") {
        val fEmulateApi = fFeatureProvider.getSync<FEmulateFeatureApi>() ?: run {
            error { "#onStartEmulateInternal could not get emulate api" }
            return@withLockResult WidgetState.NOT_INITIALIZE
        }
        val emulateHelper = fEmulateApi.getEmulateHelper()
        val state = stateMap[widgetId]
        info { "Get widget state for $widgetId, state: $state" }
        val widgetData = widgetDataApi.getWidgetDataByWidgetId(widgetId)
        val widgetKeyPath = widgetData?.flipperKeyPath
        if (widgetKeyPath != null) {
            info { "Widget key path for $widgetId is $widgetKeyPath" }
            val currentActiveEmulating = emulateHelper.getCurrentEmulatingKey().value
            if (currentActiveEmulating?.keyPath == widgetKeyPath.path) {
                info {
                    "Current active emulating is $currentActiveEmulating, so return IN_PROGRESS"
                }
                return@withLockResult WidgetState.IN_PROGRESS
            }
        }

        return@withLockResult state ?: WidgetState.PENDING
    }
}
