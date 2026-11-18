package dev.namelessnanashi.flipperzero.connection.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.bottombar.model.TabState

interface ConnectionApi {
    @Composable
    fun getConnectionTabState(componentContext: ComponentContext): TabState

    @Composable
    fun CheckAndShowUnsupportedDialog(componentContext: ComponentContext)
}
