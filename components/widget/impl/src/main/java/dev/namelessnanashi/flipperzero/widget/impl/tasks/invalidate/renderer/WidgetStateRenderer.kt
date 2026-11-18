package dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer

import android.widget.RemoteViews
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath

interface WidgetStateRenderer {
    fun render(
        widgetId: Int,
        flipperKeyPath: FlipperKeyPath? = null
    ): RemoteViews?
}
