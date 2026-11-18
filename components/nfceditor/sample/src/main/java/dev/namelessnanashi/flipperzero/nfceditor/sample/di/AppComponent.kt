package dev.namelessnanashi.flipperzero.nfceditor.sample.di

import android.app.Application
import android.content.Context
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.nfceditor.sample.NfcEditorActivity
import dev.zacsweers.metro.DependencyGraph
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@SingleIn(AppGraph::class)
@DependencyGraph(AppGraph::class)
interface AppComponent {
    fun inject(activity: NfcEditorActivity)

    @DependencyGraph.Factory
    fun interface Factory {
        fun create(
            @Provides context: Context,
            @Provides application: Application,
            @Provides applicationParams: ApplicationParams
        ): AppComponent
    }
}
