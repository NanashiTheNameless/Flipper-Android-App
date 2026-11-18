package dev.namelessnanashi.flipperzero.core.progress

fun interface ProgressListener {
    suspend fun onProgress(current: Float)
}
