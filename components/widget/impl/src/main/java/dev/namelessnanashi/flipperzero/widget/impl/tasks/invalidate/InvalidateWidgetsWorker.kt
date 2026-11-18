package dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.widget.impl.di.WidgetComponent
import dev.zacsweers.metro.Inject

class InvalidateWidgetsWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params), LogTagProvider {
    override val TAG = "InvalidateWidgetsTask"

    @Inject
    lateinit var invalidateWidgetsHelper: InvalidateWidgetsHelper

    init {
        ComponentHolder.component<WidgetComponent>().inject(this)
    }

    private val widgetNotificationHelper = WidgetNotificationHelper(context)

    override suspend fun doWork(): Result {
        invalidateWidgetsHelper.invoke()
        return Result.success()
    }

    override suspend fun getForegroundInfo() = widgetNotificationHelper.invalidateForegroundInfo(id)
}
