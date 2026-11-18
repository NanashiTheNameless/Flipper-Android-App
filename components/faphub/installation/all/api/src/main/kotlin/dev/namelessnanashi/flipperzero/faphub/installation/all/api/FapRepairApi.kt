package dev.namelessnanashi.flipperzero.faphub.installation.all.api

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem

data class FapRepairReport(
    val checkedCount: Int,
    val broken: List<FapManifestItem>,
    // Installed apps that are not in the catalog or whose version can no longer be downloaded
    val uncheckable: List<FapManifestItem>
)

interface FapRepairApi {
    /**
     * Compares every installed catalog app on the Flipper with the catalog build of the
     * same version: missing .fap files or MD5 mismatches count as broken.
     */
    suspend fun check(): FapRepairReport

    /**
     * Queues a reinstall (current catalog version) for each broken app.
     * @return how many apps were queued
     */
    suspend fun repair(broken: List<FapManifestItem>): Int
}
