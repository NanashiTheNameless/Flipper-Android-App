package dev.namelessnanashi.flipperzero.faphub.dao.api

import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import java.io.File

interface FapDownloadApi {
    suspend fun downloadBundle(
        target: FlipperTarget.Received,
        versionUid: String,
        listener: ProgressListener? = null
    ): File
}
