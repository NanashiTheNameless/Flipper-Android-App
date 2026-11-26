package dev.namelessnanashi.flipperzero.faphub.dao.network.api

import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.core.progress.ProgressWrapperTracker
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.api.FapNetworkBundleApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.call.body
import io.ktor.client.statement.HttpResponse
import io.ktor.http.isSuccess
import io.ktor.http.contentLength
import io.ktor.utils.io.ByteReadChannel
import io.ktor.utils.io.readRemaining
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import kotlinx.io.readByteArray
import java.io.File
import java.io.IOException
import java.util.concurrent.ConcurrentHashMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

private const val BUNDLE_CACHE_DIR = "fap_bundles"
private const val ABANDONED_BUNDLE_AGE_MS = 24L * 60 * 60 * 1000

// The install queue prefetches bundles while another app uploads, so the same bundle can be
// requested twice at once; one lock per bundle file keeps them from sharing a .part file.
// Process-wide: the class was not a singleton, so a per-instance map gave the prefetch and the
// install separate locks, and the second download deleted the first one's .part file
// ("incomplete: 0 of N bytes", the install failed and had to be tapped again).
private val bundleLocks = ConcurrentHashMap<String, Mutex>()

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FapDownloadApi>())
class FapDownloadApiImpl @Inject constructor(
    private val bundleApi: FapNetworkBundleApi,
    private val storageProvider: FlipperStorageProvider
) : FapDownloadApi, LogTagProvider {
    override val TAG = "FapDownloadApi"


    /**
     * Downloads the whole bundle into the cache before anything is uploaded. A bundle is only
     * kept once it is complete, so a dropped connection never yields a truncated .fap.
     */
    override suspend fun downloadBundle(
        target: FlipperTarget.Received,
        versionUid: String,
        listener: ProgressListener?
    ): File {
        val cacheDir = storageProvider.getCacheFolder(BUNDLE_CACHE_DIR).toFile()
        val bundleFile = bundleFile(cacheDir, target, versionUid)
        pruneAbandonedBundles(cacheDir, bundleFile)
        return bundleLocks.getOrPut(bundleFile.name) { Mutex() }.withLock {
            downloadBundleLocked(bundleFile, cacheDir, target, versionUid, listener)
        }
    }

    override suspend fun discardBundle(target: FlipperTarget.Received, versionUid: String) {
        val cacheDir = storageProvider.getCacheFolder(BUNDLE_CACHE_DIR).toFile()
        val bundleFile = bundleFile(cacheDir, target, versionUid)
        bundleLocks.getOrPut(bundleFile.name) { Mutex() }.withLock {
            bundleFile.delete()
            File(cacheDir, bundleFile.name + ".part").delete()
        }
    }

    private fun bundleFile(cacheDir: File, target: FlipperTarget.Received, versionUid: String): File {
        require(versionUid.matches(Regex("[A-Za-z0-9_-]{1,128}")) && target.target.matches(Regex("[A-Za-z0-9_-]{1,20}"))) {
            "Invalid bundle cache identity"
        }
        return File(cacheDir, "${versionUid}_${target.target}_${target.sdk}.fap")
    }

    private fun pruneAbandonedBundles(cacheDir: File, requested: File) {
        val cutoff = System.currentTimeMillis() - ABANDONED_BUNDLE_AGE_MS
        cacheDir.listFiles()?.filter { it.isFile && it != requested && it.lastModified() < cutoff }?.forEach { file ->
            val key = file.name.removeSuffix(".part")
            val lock = bundleLocks.getOrPut(key) { Mutex() }
            if (lock.tryLock()) {
                try { if (file.lastModified() < cutoff) file.delete() } finally { lock.unlock() }
            }
        }
    }

    private suspend fun downloadBundleLocked(
        bundleFile: File,
        cacheDir: File,
        target: FlipperTarget.Received,
        versionUid: String,
        listener: ProgressListener?
    ): File {
        if (bundleFile.length() > 0) {
            bundleFile.setLastModified(System.currentTimeMillis())
            info { "Use cached bundle for $versionUid: ${bundleFile.path}" }
            listener?.onProgress(1f)
            return bundleFile
        }
        info { "Start download bundle for $versionUid and $target" }

        val partFile = File(cacheDir, bundleFile.name + ".part")
        partFile.delete()
        try {
            bundleApi
                .downloadBundle(versionUid, target.target, target.sdk.toString())
                .execute { response ->
                    if (!response.status.isSuccess()) {
                        throw IOException("Bundle download for $versionUid failed: ${response.status}")
                    }
                    val expectedLength = response.contentLength()
                    response.saveToFile(partFile, listener?.let { ProgressWrapperTracker(it) })
                    if (partFile.length() == 0L ||
                        (expectedLength != null && partFile.length() != expectedLength)
                    ) {
                        throw IOException(
                            "Bundle download for $versionUid incomplete: " +
                                "${partFile.length()} of ${expectedLength ?: "unknown"} bytes"
                        )
                    }
                }
            if (!partFile.renameTo(bundleFile)) {
                throw IOException("Could not move ${partFile.path} to ${bundleFile.path}")
            }
        } finally {
            partFile.delete()
        }

        info { "Complete download for $versionUid (${bundleFile.length()} bytes)" }

        return bundleFile
    }
}

private suspend fun HttpResponse.saveToFile(
    file: File,
    listener: ProgressWrapperTracker?
) {
    val channel: ByteReadChannel = body()
    val totalBytes = contentLength()
    var written = 0L
    // One stream for the whole body: upstream reopened and stat'ed the file for every packet
    file.outputStream().buffered().use { output ->
        while (!channel.isClosedForRead) {
            val packet = channel.readRemaining(DEFAULT_BUFFER_SIZE.toLong())
            while (!packet.exhausted()) {
                val bytes = packet.readByteArray()
                output.write(bytes)
                written += bytes.size
                if (totalBytes != null && totalBytes > 0) {
                    listener?.onProgress(written, totalBytes)
                }
            }
        }
    }
}
