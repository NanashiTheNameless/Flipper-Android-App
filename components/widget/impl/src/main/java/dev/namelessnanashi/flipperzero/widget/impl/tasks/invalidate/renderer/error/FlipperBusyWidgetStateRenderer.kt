package dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer.error

import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyscreen.api.DeepLinkOpenKey
import dev.namelessnanashi.flipperzero.widget.impl.R
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetRendererOf
import dev.namelessnanashi.flipperzero.widget.impl.model.WidgetState
import dev.namelessnanashi.flipperzero.widget.impl.tasks.invalidate.renderer.WidgetStateRenderer
import dev.zacsweers.metro.ContributesIntoMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@WidgetRendererOf(WidgetState.ERROR_FLIPPER_BUSY)
@ContributesIntoMap(AppGraph::class, binding<WidgetStateRenderer>())
class FlipperBusyWidgetStateRenderer @Inject constructor(
    context: Context,
    deepLinkOpenKey: DeepLinkOpenKey
) : RetryErrorWidgetStateRenderer(context, deepLinkOpenKey, R.string.widget_err_busy) {
    override val TAG = "FlipperBusyWidgetStateRenderer"
}
