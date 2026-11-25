package dev.namelessnanashi.flipperzero.singleactivity.impl

import android.content.Context
import android.content.Intent
import android.os.Build
import android.hardware.usb.UsbManager
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.imePadding
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.SideEffect
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.arkivanov.decompose.extensions.compose.stack.animation.LocalStackAnimationProvider
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUsbFlipperSwitcherApi
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ktx.android.toFullString
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperTheme
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.viewmodel.ThemeViewModel
import dev.namelessnanashi.flipperzero.deeplink.api.DeepLinkParser
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.api.RootDecomposeComponent
import dev.namelessnanashi.flipperzero.singleactivity.impl.di.SingleActivityComponent
import dev.namelessnanashi.flipperzero.singleactivity.impl.utils.FlipperStackAnimationProvider
import dev.namelessnanashi.flipperzero.singleactivity.impl.utils.OnCreateHandlerDispatcher
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

class SingleActivity : AppCompatActivity(), LogTagProvider {
    override val TAG = "SingleActivity"

    @Inject
    lateinit var rootComponentFactory: RootDecomposeComponent.Factory

    @Inject
    lateinit var onCreateHandlerDispatcher: OnCreateHandlerDispatcher

    @Inject
    lateinit var deeplinkParser: DeepLinkParser

    @Inject
    lateinit var themeViewModelProvider: Provider<ThemeViewModel>

    @Inject
    lateinit var usbFlipperSwitcher: FUsbFlipperSwitcherApi

    private var rootDecomposeComponent: RootDecomposeComponent? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        ComponentHolder.component<SingleActivityComponent>().inject(this)

        onCreateHandlerDispatcher.onCreate()
        handleUsbAttached(intent)

        info {
            "Create new activity with hashcode: ${this.hashCode()} " + "and intent ${intent.toFullString()}"
        }

        enableEdgeToEdge()
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            // No grey scrim behind the 3-button navigation bar; the app's own bottom bar shows
            // through, black in the dark theme
            window.isNavigationBarContrastEnforced = false
        }

        val root = rootComponentFactory(
            componentContext = defaultComponentContext(),
            onBack = this::finish,
            initialDeeplink = runBlocking {
                deeplinkParser.parseOrLog(this@SingleActivity, intent)
            }
        ).also { rootDecomposeComponent = it }

        setContent {
            FlipperTheme(
                content = {
                    // enableEdgeToEdge picks icon colours from the system theme, not the app theme;
                    // with dark headers that left the status bar icons invisible
                    val view = LocalView.current
                    SideEffect {
                        WindowCompat.getInsetsController(window, view).apply {
                            isAppearanceLightStatusBars = false
                            isAppearanceLightNavigationBars = false
                        }
                    }
                    CompositionLocalProvider(
                        LocalRootNavigation provides root,
                        LocalDeeplinkHandler provides root,
                        LocalStackAnimationProvider provides FlipperStackAnimationProvider
                    ) {
                        root.Render(
                            modifier = Modifier
                                .fillMaxSize()
                                .imePadding()
                                .background(LocalPallet.current.background)
                        )
                    }
                },
                themeViewModel = root.viewModelWithFactory(key = null) {
                    themeViewModelProvider.invoke()
                }
            )
        }
    }

    @Suppress("UNNECESSARY_SAFE_CALL", "UnnecessarySafeCall", "SENSELESS_COMPARISON")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        handleUsbAttached(intent)
        info { "Receive new intent: ${intent?.toFullString()}" }
        if (intent == null) {
            return
        }
        lifecycleScope.launch(FlipperDispatchers.workStealingDispatcher) {
            deeplinkParser.parseOrLog(this@SingleActivity, intent)?.let {
                rootDecomposeComponent?.handleDeeplink(it)
            }
        }
    }

    private suspend fun DeepLinkParser.parseOrLog(context: Context, intent: Intent): Deeplink? {
        return try {
            fromIntent(context, intent)
        } catch (throwable: Exception) {
            error(throwable) { "Failed parse deeplink" }
            null
        }
    }

    private fun handleUsbAttached(intent: Intent?) {
        // The system opens the app when a Flipper is plugged in (see usb_device_filter.xml)
        if (intent?.action == UsbManager.ACTION_USB_DEVICE_ATTACHED) {
            usbFlipperSwitcher.rescan()
        }
    }

}
