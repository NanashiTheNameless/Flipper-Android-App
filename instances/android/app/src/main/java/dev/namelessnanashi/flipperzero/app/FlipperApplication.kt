package dev.namelessnanashi.flipperzero.app

import android.app.Application
import coil3.ImageLoader
import coil3.PlatformContext
import coil3.SingletonImageLoader
import coil3.request.crossfade
import coil3.svg.SvgDecoder
import dev.namelessnanashi.flipperzero.app.di.AppComponent
import dev.zacsweers.metro.createGraphFactory
import dev.namelessnanashi.flipperzero.app.di.MainComponent
import dev.namelessnanashi.flipperzero.core.activityholder.CurrentActivityHolder
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.singleactivity.impl.SingleActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import timber.log.Timber

class FlipperApplication : Application(), SingletonImageLoader.Factory, LogTagProvider {
    override val TAG = "FlipperApplication"

    private val applicationScope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    override fun onCreate() {
        super.onCreate()

        CurrentActivityHolder.register(this)

        val appComponent = createGraphFactory<AppComponent.Factory>()
            .create(
                context = this,
                application = this,
                scope = applicationScope,
                ApplicationParams(
                    startApplicationClass = SingleActivity::class,
                    version = BuildKonfig.VERSION_NAME
                )
            )

        ComponentHolder.components += appComponent

        if (BuildKonfig.IS_LOG_ENABLED) {
            Timber.plant(Timber.DebugTree())
        }
        setUp()

        info { "Start Flipper Application with version ${BuildKonfig.VERSION_NAME}" }
    }

    private fun setUp() {
        val component = ComponentHolder.component<MainComponent>()
        val synchronizationApi by component.synchronizationApi
        synchronizationApi.startSynchronization()
        component.permissionRequestHandlerImpl.invoke().register(this)
        component.fConnectionService.invoke().onApplicationInit()
        component.usbFlipperSwitcher.invoke().start()
        component.otherFlipperAppWarning.invoke().start()
        component.connectionKeepAlive.invoke().start()
    }

    override fun newImageLoader(context: PlatformContext): ImageLoader {
        return ImageLoader.Builder(context.applicationContext)
            .components {
                add(SvgDecoder.Factory())
            }
            .crossfade(true)
            .build()
    }
}
