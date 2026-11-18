package dev.namelessnanashi.flipperzero.faphub.dao.api

interface FapReportApi {
    suspend fun report(applicationUid: String, description: String)
}
