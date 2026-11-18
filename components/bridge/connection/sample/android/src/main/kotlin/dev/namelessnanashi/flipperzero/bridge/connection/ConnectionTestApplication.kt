package dev.namelessnanashi.flipperzero.bridge.connection

import android.app.Application
import dev.namelessnanashi.flipperzero.bridge.connection.di.AppComponent
import dev.namelessnanashi.flipperzero.bridge.connection.di.AndroidAppComponent
import dev.zacsweers.metro.createGraphFactory
import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import timber.log.Timber

class ConnectionTestApplication : Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = createGraphFactory<AndroidAppComponent.Factory>()
            .create(
                context = this,
                application = this,
                ApplicationParams(
                    startApplicationClass = ConnectionTestActivity::class,
                    version = "Sample"
                )
            )

        ComponentHolder.components += appComponent

        Timber.plant(Timber.DebugTree())
    }
}
