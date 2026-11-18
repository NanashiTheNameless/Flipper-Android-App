package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.actions

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import java.util.Base64
import dev.zacsweers.metro.Inject

class FapIconDownloader @Inject constructor(
    private val client: HttpClient
) : LogTagProvider {
    override val TAG = "FapIconDownloader"

    suspend fun downloadToBase64(picUrl: String): Result<String> = runCatching {
        info { "Download $picUrl" }
        val imageBytes = client.get(picUrl).body<ByteArray>()
        return@runCatching Base64.getEncoder().encodeToString(imageBytes)
    }
}
