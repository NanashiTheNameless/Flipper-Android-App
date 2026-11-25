package dev.namelessnanashi.flipperzero.faphub.installation.all.api

import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem

data class FapRepairReport(
    val checkedCount: Int,
    val broken: List<FapManifestItem>,
    // Installed apps that are not in the catalog or whose version can no longer be downloaded
    val uncheckable: List<FapManifestItem>
)

enum class FapRepairStage { PREPARING, CHECKING, DOWNLOADING, QUEUING }

data class FapRepairProgress(
    val completed: Int = 0,
    val total: Int = 0,
    val appName: String? = null,
    val stage: FapRepairStage = FapRepairStage.PREPARING,
    val downloadProgress: Float = 0f
) {
    val fraction: Float get() = if (total == 0) 0f else
        ((completed + if (stage == FapRepairStage.DOWNLOADING) downloadProgress else 0f) / total).coerceIn(0f, 1f)
}

interface FapRepairApi {
    /**
     * Compares every installed catalog app on the Flipper with the catalog build of the
     * same version: missing .fap files or MD5 mismatches count as broken.
     */
    suspend fun check(onProgress: suspend (FapRepairProgress) -> Unit = {}): FapRepairReport

    /**
     * Queues a reinstall (current catalog version) for each broken app.
     * @return how many apps were queued
     */
    suspend fun repair(broken: List<FapManifestItem>, onProgress: suspend (FapRepairProgress) -> Unit = {}): Int
}
