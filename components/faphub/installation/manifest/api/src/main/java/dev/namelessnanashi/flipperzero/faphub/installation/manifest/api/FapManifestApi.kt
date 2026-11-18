package dev.namelessnanashi.flipperzero.faphub.installation.manifest.api

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import kotlinx.coroutines.flow.StateFlow

interface FapManifestApi {
    fun getManifestFlow(): StateFlow<FapManifestState>
    suspend fun add(pathToFap: String, fapManifestItem: FapManifestItem)
    suspend fun remove(applicationUid: String)
    fun invalidateAsync()
}
