package dev.namelessnanashi.flipperzero.nfceditor.sample

import android.app.Application
import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.nfceditor.sample.di.AppComponent
import dev.zacsweers.metro.createGraphFactory
import timber.log.Timber

class NfcEditorApplication : Application() {
    companion object {
        lateinit var appComponent: AppComponent
    }

    override fun onCreate() {
        super.onCreate()
        appComponent = createGraphFactory<AppComponent.Factory>()
            .create(
                context = this,
                application = this,
                ApplicationParams(
                    startApplicationClass = NfcEditorActivity::class,
                    version = "Sample"
                )
            )

        Timber.plant(Timber.DebugTree())
    }
}
