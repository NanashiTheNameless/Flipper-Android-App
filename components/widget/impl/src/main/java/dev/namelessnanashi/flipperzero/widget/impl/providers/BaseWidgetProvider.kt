package dev.namelessnanashi.flipperzero.widget.impl.providers

import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.WidgetType
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ktx.android.toFullString
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.widget.impl.di.WidgetComponent

abstract class BaseWidgetProvider(
    private val widgetType: WidgetType
) : AppWidgetProvider(), LogTagProvider {
    override val TAG = "BaseWidgetProvider-$widgetType"

    override fun onReceive(context: Context, intent: Intent?) {
        super.onReceive(context, intent)
        info { "Call ${intent?.toFullString()} for $widgetType" }
        runBlockingWithLog("update") {
            ComponentHolder.component<WidgetComponent>().invalidateWidgetsHelper.invoke()
        }
    }

    override fun onDeleted(context: Context?, appWidgetIds: IntArray?) {
        super.onDeleted(context, appWidgetIds)
        info { "Deleted ${appWidgetIds?.toList()}" }
        runBlockingWithLog("deleted") {
            val widgetDataApi = ComponentHolder.component<WidgetComponent>().widgetDataApi
            appWidgetIds?.forEach { id ->
                widgetDataApi.deleteWidget(id)
            }
        }
    }
}
