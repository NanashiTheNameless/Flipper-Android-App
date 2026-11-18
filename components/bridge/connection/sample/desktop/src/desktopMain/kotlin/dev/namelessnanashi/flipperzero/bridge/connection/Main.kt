package dev.namelessnanashi.flipperzero.bridge.connection

import dev.namelessnanashi.flipperzero.bridge.connection.di.DesktopAppComponent
import dev.zacsweers.metro.createGraphFactory
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob

fun main() {
    val applicationScope = CoroutineScope(
        SupervisorJob() + FlipperDispatchers.workStealingDispatcher
    )
    // Always create the root component outside Compose on the UI thread
    val appComponent = createGraphFactory<DesktopAppComponent.Factory>()
        .create(
            scope = applicationScope
        )

    launch(appComponent)
}
