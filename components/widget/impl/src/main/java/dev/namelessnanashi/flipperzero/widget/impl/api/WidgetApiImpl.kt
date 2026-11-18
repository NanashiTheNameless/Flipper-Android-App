package dev.namelessnanashi.flipperzero.widget.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.widget.api.WidgetApi
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetState
import dev.namelessnanashi.flipperzero.widget.impl.storage.WidgetStateStorage
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.InvalidateWidgetsHelper
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<WidgetApi>())
class WidgetApiImpl @Inject constructor(
    private val invalidateWidgetsHelper: InvalidateWidgetsHelper,
    private val updateStorage: WidgetStateStorage
) : WidgetApi {
    override suspend fun invalidate() {
        invalidateWidgetsHelper.invoke()
    }

    override suspend fun resetStateOfWidget(widgetId: Int) {
        updateStorage.updateState(widgetId, WidgetState.PENDING)
        invalidate()
    }
}
