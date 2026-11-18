package dev.namelessnanashi.flipperzero.faphub.installedtab.api

import kotlinx.coroutines.flow.Flow

interface FapUpdatePendingCountApi {
    fun getUpdatePendingCount(): Flow<Int>
}
