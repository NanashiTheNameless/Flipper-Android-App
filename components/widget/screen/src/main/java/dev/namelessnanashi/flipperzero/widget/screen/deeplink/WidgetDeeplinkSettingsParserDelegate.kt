package dev.namelessnanashi.flipperzero.widget.screen.deeplink

import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParserDelegate
import dev.namelessnanashi.flipperzero.deeplink.model.DeepLinkParserDelegatePriority
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.zacsweers.metro.ContributesIntoSet
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val EXTRA_ID_WIDGET_INVALID = -1

@ContributesIntoSet(AppGraph::class, binding<DeepLinkParserDelegate>())
class WidgetDeeplinkSettingsParserDelegate @Inject constructor() :
    DeepLinkParserDelegate,
    LogTagProvider {
    override val TAG = "WidgetDeeplinkSettingsParserDelegate"

    override fun getPriority(context: Context, intent: Intent): DeepLinkParserDelegatePriority? {
        return if (isWidgetOptionsIntent(intent)) {
            DeepLinkParserDelegatePriority.HIGH
        } else {
            null
        }
    }

    override suspend fun fromIntent(context: Context, intent: Intent): Deeplink? {
        if (!isWidgetOptionsIntent(intent)) return null
        val widgetId = intent.getIntExtra(
            AppWidgetManager.EXTRA_APPWIDGET_ID,
            EXTRA_ID_WIDGET_INVALID
        )
        if (widgetId < 0) {
            return null
        }
        return Deeplink.RootLevel.WidgetOptions(widgetId)
    }

    private fun isWidgetOptionsIntent(intent: Intent): Boolean {
        return intent.action == AppWidgetManager.ACTION_APPWIDGET_CONFIGURE &&
            intent.extras?.containsKey(AppWidgetManager.EXTRA_APPWIDGET_ID) == true
    }
}
