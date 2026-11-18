package dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer.error

import android.app.PendingIntent
import android.content.Context
import android.widget.RemoteViews
import androidx.annotation.StringRes
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.keyscreen.api.DeepLinkOpenKey
import dev.namelessnanashi.flipperzero.widget.impl.R
import dev.namelessnanashi.flipperzero.widget.impl.broadcast.WidgetBroadcastReceiver
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer.WidgetStateRenderer

abstract class RetryErrorWidgetStateRenderer(
    private val context: Context,
    private val deepLinkOpenKey: DeepLinkOpenKey,
    @StringRes private val errorTextId: Int
) : WidgetStateRenderer, LogTagProvider {
    override fun render(widgetId: Int, flipperKeyPath: FlipperKeyPath?): RemoteViews? {
        val text = context.getString(errorTextId)
        info { "#render $widgetId for $flipperKeyPath with text: $text" }
        val keyPath = flipperKeyPath ?: return null

        return RemoteViews(context.packageName, R.layout.widget_layout_error).apply {
            setTextViewText(R.id.button_txt, context.getString(R.string.widget_err_retry_btn))
            setTextViewText(R.id.error_text, text)

            val startIntent = WidgetBroadcastReceiver.buildStartIntent(
                context,
                keyPath,
                widgetId
            )
            setOnClickPendingIntent(R.id.button, startIntent)
            setOnClickPendingIntent(
                R.id.error_btn,
                PendingIntent.getActivity(
                    context,
                    widgetId,
                    deepLinkOpenKey.getIntentForOpenKey(flipperKeyPath),
                    PendingIntent.FLAG_IMMUTABLE or PendingIntent.FLAG_UPDATE_CURRENT
                )
            )
        }
    }
}
