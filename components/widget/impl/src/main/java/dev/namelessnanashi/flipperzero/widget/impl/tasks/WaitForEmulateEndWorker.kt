package dev.namelessnanashi.flipperzero.widget.impl.tasks

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.namelessnanashi.flipperzero.bridge.connection.feature.emulate.api.FEmulateFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import com.flipperdevices.protobuf.app.AppState
import dev.namelessnanashi.flipperzero.widget.impl.di.WidgetComponent
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.WidgetNotificationHelper
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject

class WaitForEmulateEndWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), LogTagProvider {
    override val TAG = "WaitForEmulateEndWorker"

    @Inject
    lateinit var fFeatureProvider: FFeatureProvider

    init {
        ComponentHolder.component<WidgetComponent>().inject(this)
    }

    private val widgetNotificationHelper = WidgetNotificationHelper(context)

    override suspend fun doWork(): Result {
        waitEmulateEnd()
        return Result.success()
    }

    private suspend fun waitEmulateEnd() = combine(
        flow = fFeatureProvider.get<FEmulateFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FEmulateFeatureApi> }
            .map { status -> status?.featureApi }
            .flatMapLatest { feature ->
                feature?.getEmulateHelper()?.getCurrentEmulatingKey() ?: flowOf(null)
            },
        flow2 = fFeatureProvider.get<FEmulateFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FEmulateFeatureApi> }
            .map { status -> status?.featureApi }
            .flatMapLatest { feature ->
                feature?.getAppEmulateHelper()?.appStateFlow() ?: flowOf(null)
            },
        transform = { currentEmulatingKey, appStateResponse ->
            currentEmulatingKey to appStateResponse
        }

    ).filter { (currentEmulatingKey, appStateResponse) ->
        return@filter when {
            currentEmulatingKey == null -> true
            appStateResponse?.state == AppState.APP_CLOSED -> true
            else -> false
        }
    }.first()

    override suspend fun getForegroundInfo() = widgetNotificationHelper.emulatingForegroundInfo(id)
}
