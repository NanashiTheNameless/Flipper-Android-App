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

    /** Releases a downloaded bundle once installation has succeeded or its prefetch was abandoned. */
    suspend fun discardBundle(target: FlipperTarget.Received, versionUid: String)
}
