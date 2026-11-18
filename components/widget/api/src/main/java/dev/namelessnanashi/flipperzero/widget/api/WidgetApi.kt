package dev.namelessnanashi.flipperzero.widget.api

interface WidgetApi {
    suspend fun invalidate()
    suspend fun resetStateOfWidget(widgetId: Int)
}
