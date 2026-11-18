package dev.namelessnanashi.flipperzero.faphub.installation.all.api

interface FapInstallationAllApi {
    /**
     * Queues every catalog app that is not installed, not hidden and installable on this Flipper.
     * @return how many apps were queued
     */
    suspend fun installAll(): Int
}
