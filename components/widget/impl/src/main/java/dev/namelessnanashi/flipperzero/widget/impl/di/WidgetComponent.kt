package dev.namelessnanashi.flipperzero.widget.impl.di

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.WidgetDataApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.widget.impl.providers.BaseWidgetProvider
import dev.namelessnanashi.flipperzero.widget.impl.storage.WidgetStateStorage
import dev.namelessnanashi.flipperzero.widget.impl.tasks.StartEmulateWorker
import dev.namelessnanashi.flipperzero.widget.impl.tasks.StopEmulateWorker
import dev.namelessnanashi.flipperzero.widget.impl.tasks.WaitForEmulateEndWorker
import dev.namelessnanashi.flipperzero.widget.impl.tasks.WaitingForFlipperConnectWorker
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.InvalidateWidgetsHelper
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.InvalidateWidgetsWorker
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppGraph::class)
interface WidgetComponent {
    val widgetDataApi: WidgetDataApi
    val widgetStateStorage: WidgetStateStorage
    val invalidateWidgetsHelper: InvalidateWidgetsHelper

    fun inject(worker: StartEmulateWorker)
    fun inject(worker: StopEmulateWorker)
    fun inject(worker: InvalidateWidgetsWorker)
    fun inject(worker: WaitForEmulateEndWorker)
    fun inject(worker: WaitingForFlipperConnectWorker)
    fun inject(provider: BaseWidgetProvider)
}
