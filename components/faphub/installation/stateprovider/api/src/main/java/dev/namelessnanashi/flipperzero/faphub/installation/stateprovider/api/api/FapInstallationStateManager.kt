package dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.api

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion
import dev.namelessnanashi.flipperzero.faphub.installation.stateprovider.api.model.FapState
import kotlinx.coroutines.flow.Flow

interface FapInstallationStateManager {
    fun getFapStateFlow(
        applicationUid: String,
        currentVersion: FapItemVersion
    ): Flow<FapState>
}
