package dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.content.Context
import android.content.Intent
import android.widget.RemoteViews
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.widget.impl.R
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetRendererOf
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetState
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@WidgetRendererOf(WidgetState.NOT_INITIALIZE)
@ContributesIntoMap(AppGraph::class, binding<WidgetStateRenderer>())
class NotInitializeWidgetStateRenderer @Inject constructor(
    private val context: Context,
    private val applicationParams: ApplicationParams
) : WidgetStateRenderer, LogTagProvider {
    override val TAG = "NotInitializeWidgetStateRenderer"

    override fun render(widgetId: Int, flipperKeyPath: FlipperKeyPath?): RemoteViews {
        val intent = Intent(context, applicationParams.startApplicationClass.java)
        intent.action = AppWidgetManager.ACTION_APPWIDGET_CONFIGURE
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId)

        val configurePendingIntent = PendingIntent.getActivity(
            context,
            widgetId,
            intent,
            PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
        )
        return RemoteViews(context.packageName, R.layout.initial_layout).apply {
            setOnClickPendingIntent(
                R.id.widget_root,
                configurePendingIntent
            )
        }
    }
}
