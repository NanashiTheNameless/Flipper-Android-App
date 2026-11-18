package dev.namelessnanashi.flipperzero.core.progress

fun interface FixedProgressListener {
    suspend fun onProgress(current: Long, max: Long)
}
