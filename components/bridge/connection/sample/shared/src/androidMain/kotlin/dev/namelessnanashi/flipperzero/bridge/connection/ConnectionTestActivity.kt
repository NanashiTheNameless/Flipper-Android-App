package dev.namelessnanashi.flipperzero.bridge.connection

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.defaultComponentContext
import dev.namelessnanashi.flipperzero.bridge.connection.di.AppComponent
import dev.namelessnanashi.flipperzero.bridge.connection.screens.ConnectionRootDecomposeComponent
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperTheme
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.viewmodel.ThemeViewModel
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

class ConnectionTestActivity : AppCompatActivity() {
    @Inject
    lateinit var themeViewModelProvider: Provider<ThemeViewModel>

    @Inject
    lateinit var rootComponentFactory: ConnectionRootDecomposeComponent.Factory

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentHolder.component<AppComponent>().inject(this)

        enableEdgeToEdge()

        val root = rootComponentFactory(
            componentContext = defaultComponentContext(),
        )

        setContent {
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
                    themeViewModelProvider.invoke()
                }
            )
        }
    }
}
