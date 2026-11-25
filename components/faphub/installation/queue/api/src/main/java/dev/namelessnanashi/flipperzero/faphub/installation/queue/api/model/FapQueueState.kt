package dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model

/**
 * Install/update progress up to this value is the download of the app from the catalog; the
 * rest is the upload to the Flipper (fork: lets the UI show the two steps separately)
 */
const val FAP_DOWNLOAD_PROGRESS_SHARE = 0.1f

sealed class FapQueueState {
    data object NotFound : FapQueueState()

    data class Pending(
        val request: FapActionRequest,
        val downloadProgress: Float = 0f
    ) : FapQueueState()

    data class InProgress(
        val request: FapActionRequest,
        val float: Float
    ) : FapQueueState()

    data class Failed(
        val request: FapActionRequest,
        val throwable: Throwable
    ) : FapQueueState()
}
