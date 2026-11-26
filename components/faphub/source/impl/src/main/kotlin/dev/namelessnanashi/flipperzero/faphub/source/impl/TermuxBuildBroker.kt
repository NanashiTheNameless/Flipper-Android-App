package dev.namelessnanashi.flipperzero.faphub.source.impl

import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapArtifact
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch
import kotlinx.serialization.json.buildJsonObject
import kotlinx.serialization.json.put
import java.io.Closeable
import java.io.File
import java.net.InetAddress
import java.net.ServerSocket
import java.net.Socket
import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Collections

internal data class BuiltApp(val appId: String, val category: String, val builderVersion: String, val toolchainVersion: String)

/** One job, one loopback port, one random capability; no shared-storage permissions. */
internal class TermuxBuildBroker(
    private val record: SourceRecord,
    private val directory: File,
    private val onLog: (String) -> Unit
) : Closeable {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)
    private val server = ServerSocket(0, 4, InetAddress.getByName("127.0.0.1"))
    private val clients = Collections.synchronizedSet(mutableSetOf<Socket>())
    val token = ByteArray(32).also { SecureRandom().nextBytes(it) }.joinToString("") { "%02x".format(it) }
    val url = "http://127.0.0.1:${server.localPort}"
    val connected = CompletableDeferred<Unit>()
    val result = CompletableDeferred<BuiltApp>()
    val capabilities = CompletableDeferred<String>()
    val stopped = CompletableDeferred<Unit>()
    @Volatile var launched = false
    @Volatile private var cancelled = false

    init {
        scope.launch {
            while (!server.isClosed) {
                val socket = runCatching { server.accept() }.getOrNull() ?: break
                if (clients.size >= 4) { socket.close(); continue }
                clients.add(socket)
                launch {
                    socket.use {
                        runCatching { serve(socket) }
                    }
                    clients.remove(socket)
                }
            }
        }
    }

    private fun serve(socket: Socket) {
        socket.soTimeout = 20_000
        val input = socket.getInputStream().buffered()
        val output = socket.getOutputStream().buffered()
        fun line(): String {
            val bytes = ArrayList<Byte>()
            while (true) {
                val byte = input.read()
                check(byte >= 0) { "Incomplete request" }
                if (byte == 10) break
                require(bytes.size < 4096) { "Request header too long" }
                if (byte != 13) bytes.add(byte.toByte())
            }
            return bytes.toByteArray().toString(Charsets.UTF_8)
        }
        val request = line().split(' ')
        require(request.size == 3)
        val headers = mutableMapOf<String, String>()
        while (true) {
            val header = line()
            if (header.isEmpty()) break
            require(headers.size < 32) { "Too many headers" }
            val key = header.substringBefore(':').lowercase()
            require(key !in headers) { "Duplicate HTTP header" }
            headers[key] = header.substringAfter(':').trim()
        }
        fun respond(code: Int, data: ByteArray = ByteArray(0)) {
            output.write("HTTP/1.1 $code Result\r\nContent-Length: ${data.size}\r\nConnection: close\r\n\r\n".toByteArray())
            output.write(data)
            output.flush()
        }
        val authentication = headers["authorization"].orEmpty().toByteArray()
        if (!MessageDigest.isEqual(authentication, "Bearer $token".toByteArray())) {
            respond(403)
            return
        }
        require("transfer-encoding" !in headers) { "Chunked requests are not supported" }
        fun payload(limit: Int): ByteArray {
            val length = headers["content-length"]?.toIntOrNull() ?: error("Content length required")
            require(length in 0..limit) { "Request body too large" }
            val data = ByteArray(length)
            var offset = 0
            while (offset < length) {
                val count = input.read(data, offset, length - offset)
                check(count > 0) { "Incomplete request body" }
                offset += count
            }
            return data
        }
        try { when (request[0] to request[1]) {
            "GET" to "/recipe" -> {
                val recipe = buildJsonObject {
                    put("protocol", 1)
                    put("sourceSha256", record.sourceSha256)
                    put("sdkSha256", record.sdkSha256)
                    put("project", listOf(record.archiveRoot, record.project).filter { it.isNotEmpty() && it != "." }.joinToString("/"))
                }.toString()
                respond(200, recipe.toByteArray())
                connected.complete(Unit)
            }
            "GET" to "/state" -> respond(200, "{\"cancelled\":$cancelled}".toByteArray())
            "GET" to "/source", "GET" to "/sdk" -> {
                val file = File(directory, if (request[1] == "/source") "source.zip" else "sdk.zip")
                require(file.isFile && file.length() in 1..SourceHttp.MAX_DOWNLOAD)
                output.write("HTTP/1.1 200 OK\r\nContent-Length: ${file.length()}\r\nConnection: close\r\n\r\n".toByteArray())
                file.inputStream().use { it.copyTo(output) }
                output.flush()
            }
            "POST" to "/log" -> {
                onLog(payload(16 * 1024).toString(Charsets.UTF_8))
                respond(200)
            }
            "POST" to "/error" -> {
                val error = payload(8192).toString(Charsets.UTF_8)
                respond(200)
                val failure = IllegalStateException(error)
                connected.completeExceptionally(failure)
                capabilities.completeExceptionally(failure)
                result.completeExceptionally(failure)
                stopped.complete(Unit)
            }
            "POST" to "/capabilities" -> {
                val data = payload(8192).toString(Charsets.UTF_8)
                respond(200)
                capabilities.complete(data)
            }
            "POST" to "/artifact" -> {
                val app = headers["x-app-id"].orEmpty()
                val category = java.net.URLDecoder.decode(headers["x-app-category"].orEmpty(), "UTF-8")
                require(app.matches(Regex("[A-Za-z0-9_-]{1,80}"))) { "Invalid built app ID" }
                FapArtifact.requireSegment(category)
                val temporary = File(directory, "artifact.fap.part")
                temporary.outputStream().use { file ->
                    file.write(payload(FapArtifact.MAX_BYTES.toInt()))
                    file.fd.sync()
                }
                FapArtifact.inspect(temporary)
                check(temporary.renameTo(File(directory, "artifact.fap")))
                respond(200)
                result.complete(BuiltApp(app, category, headers["x-builder-version"].orEmpty(), headers["x-toolchain-version"].orEmpty()))
            }
            else -> respond(404)
        } } catch (error: Exception) {
            runCatching { respond(400, error.message.orEmpty().toByteArray()) }
            result.completeExceptionally(error)
            capabilities.completeExceptionally(error)
        }
    }

    fun requestCancel() { cancelled = true }

    override fun close() {
        cancelled = true
        server.close()
        synchronized(clients) { clients.toList().forEach { runCatching { it.close() } } }
        scope.cancel()
        File(directory, "artifact.fap.part").delete()
    }
}
