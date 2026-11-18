package dev.namelessnanashi.flipperzero.bridge.connection

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.decompose.extensions.compose.lifecycle.LifecycleController
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import dev.namelessnanashi.flipperzero.bridge.connection.di.AppComponent
import dev.namelessnanashi.flipperzero.bridge.connection.utils.runOnUiThread
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperTheme
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet

fun launch(appComponent: AppComponent) {
    val lifecycle = LifecycleRegistry()
    val root = runOnUiThread {
        appComponent.rootComponentFactory(
            DefaultComponentContext(lifecycle = lifecycle)
        )
    }
    application {
        val windowState = rememberWindowState()

        LifecycleController(lifecycle, windowState)
        Window(
            onCloseRequest = ::exitApplication,
            state = windowState,
            title = "Flipper App",
        ) {
            FlipperTheme(
                content = {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(LocalPallet.current.background)
                            .safeDrawingPadding()
                    ) {
                        root.Render()
                    }
                },
                themeViewModel = root.viewModelWithFactory(key = null) {
                    appComponent.themeViewModelProvider.invoke()
                }
            )
        }
    }
}
