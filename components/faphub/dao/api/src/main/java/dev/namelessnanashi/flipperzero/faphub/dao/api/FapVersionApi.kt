package dev.namelessnanashi.flipperzero.faphub.dao.api

import dev.namelessnanashi.flipperzero.core.data.SemVer

interface FapVersionApi {
    suspend fun getVersionsMap(versions: List<String>): Map<String, SemVer>
}
