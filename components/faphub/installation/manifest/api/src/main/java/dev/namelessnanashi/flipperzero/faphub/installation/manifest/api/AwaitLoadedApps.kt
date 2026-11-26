package dev.namelessnanashi.flipperzero.faphub.installation.manifest.api

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.withTimeoutOrNull

suspend fun FapManifestApi.awaitLoadedApps(timeoutMillis: Long = 120_000): FapManifestState.Loaded {
    val state = withTimeoutOrNull(timeoutMillis) {
        getManifestFlow().first { it is FapManifestState.NotLoaded || (it is FapManifestState.Loaded && !it.inProgress) }
    } ?: error("Installed apps did not finish loading. Check the Flipper connection and try again.")
    return state as? FapManifestState.Loaded
        ?: throw IllegalStateException("Could not load installed apps. Check the Flipper connection and SD card.", (state as FapManifestState.NotLoaded).throwable)
}
