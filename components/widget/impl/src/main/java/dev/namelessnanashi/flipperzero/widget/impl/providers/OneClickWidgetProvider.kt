package dev.namelessnanashi.flipperzero.widget.impl.providers

import android.appwidget.AppWidgetManager
import android.content.Context
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.WidgetType
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.widget.impl.di.WidgetComponent

class OneClickWidgetProvider : BaseWidgetProvider(WidgetType.ONE_CLICK) {
    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        runBlockingWithLog("update_simple") {
            val widgetDataApi = ComponentHolder.component<WidgetComponent>().widgetDataApi
            appWidgetIds.forEach {
                widgetDataApi.updateTypeForWidget(it, WidgetType.ONE_CLICK)
            }
        }
        super.onUpdate(context, appWidgetManager, appWidgetIds)
    }
}
