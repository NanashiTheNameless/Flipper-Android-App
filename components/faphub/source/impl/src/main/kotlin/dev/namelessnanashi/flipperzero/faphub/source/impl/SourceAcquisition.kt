package dev.namelessnanashi.flipperzero.faphub.source.impl

import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import java.io.File
import java.net.HttpURLConnection
import java.net.URI
import java.net.URLDecoder
import java.net.URLEncoder
import java.util.zip.ZipFile

internal object SourceHttp {
    const val MAX_DOWNLOAD = 128L * 1024 * 1024

    fun https(value: String): URI {
        val uri = URI(value)
        require(uri.scheme == "https" && !uri.host.isNullOrBlank() && uri.userInfo == null &&
            uri.port in listOf(-1, 443)) { "Use a public HTTPS link without credentials" }
        return uri
    }

    private fun open(value: String): HttpURLConnection {
        var uri = https(value)
        repeat(6) {
            val connection = uri.toURL().openConnection() as HttpURLConnection
            connection.connectTimeout = 15_000
            connection.readTimeout = 30_000
            connection.instanceFollowRedirects = false
            connection.setRequestProperty("User-Agent", "Flipper-Nanashi-AppSources")
            connection.setRequestProperty("Accept", "application/vnd.github+json, application/octet-stream, */*")
            if (connection.responseCode in listOf(301, 302, 303, 307, 308)) {
                val next = connection.getHeaderField("Location") ?: error("Download redirect has no location")
                connection.disconnect()
                uri = https(uri.resolve(next).toString())
            } else {
                check(connection.responseCode in 200..299) {
                    val code = connection.responseCode
                    connection.disconnect()
                    if (code == 403 || code == 429) "Source rate limit reached. Try again later." else "Source returned HTTP $code"
                }
                return connection
            }
        }
        error("Too many download redirects")
    }

    suspend fun download(url: String, target: File, limit: Long = MAX_DOWNLOAD) {
        val connection = open(url)
        val temporary = File(target.parentFile, target.name + ".part")
        try {
            require(connection.contentLengthLong <= limit) { "Download is too large" }
            connection.inputStream.use { input ->
                temporary.outputStream().use { output ->
                    val buffer = ByteArray(64 * 1024)
                    var count = 0L
                    while (true) {
                        currentCoroutineContext().ensureActive()
                        val size = input.read(buffer)
                        if (size < 0) break
                        count += size
                        require(count <= limit) { "Download is too large" }
                        output.write(buffer, 0, size)
                    }
                    require(count > 0 && (connection.contentLengthLong < 0 || count == connection.contentLengthLong)) { "Incomplete download" }
                    output.fd.sync()
                }
            }
            check(temporary.renameTo(target)) { "Could not finalize download" }
        } finally {
            connection.disconnect()
            temporary.delete()
        }
    }

    suspend fun json(url: String, directory: File): JsonObject {
        val file = File.createTempFile("metadata-", ".json", directory)
        try {
            download(url, file, 4L * 1024 * 1024)
            return Json.parseToJsonElement(file.readText()).jsonObject
        } finally { file.delete() }
    }
}

internal data class ResolvedSource(
    val downloadUrl: String = "", val repository: String = "", val revision: String = "",
    val tracking: String = "file", val source: Boolean = false,
    val choices: List<String> = emptyList(), val filename: String = "", val repositoryId: String = ""
)

internal class SourceResolver(
    private val directory: File,
    private val metadata: suspend (String) -> JsonObject = { SourceHttp.json(it, directory) }
) {
    suspend fun resolve(value: String): ResolvedSource {
        val uri = SourceHttp.https(value)
        fun decode(value: String) = URLDecoder.decode(value.replace("+", "%2B"), "UTF-8")
        fun encode(value: String) = URLEncoder.encode(value, "UTF-8").replace("+", "%20")
        val path = uri.rawPath.trim('/').split('/').filter { it.isNotEmpty() }.map(::decode)
        if (uri.host == "github.com" && path.size >= 2) {
            val owner = path[0]
            val repo = path[1].removeSuffix(".git")
            require(owner.matches(Regex("[A-Za-z0-9_.-]+")) && repo.matches(Regex("[A-Za-z0-9_.-]+"))) { "Invalid GitHub repository" }
            val api = "https://api.github.com/repos/$owner/$repo"
            val repository = "https://github.com/$owner/$repo"
            if (path.getOrNull(2) == "releases" && path.getOrNull(3) != "download") {
                val tag = if (path.getOrNull(3) == "tag") path.drop(4).joinToString("/") else ""
                val release = metadata("$api/releases/" + if (tag.isBlank()) "latest" else "tags/${encode(tag)}")
                val assets = release["assets"]?.jsonArray.orEmpty().map { it.jsonObject }
                    .filter { it.string("name").let { name -> name.endsWith(".fap", true) || name.endsWith(".zip", true) } }
                require(assets.isNotEmpty()) { "No FAP or ZIP assets in this release. Use the repository link to build from source." }
                return ResolvedSource(
                    repository = repository, revision = release.string("tag_name"),
                    tracking = if (tag.isBlank()) "release:latest" else "release:$tag",
                    choices = assets.map { "asset:${it.string("browser_download_url")}" }
                )
            }
            val isArchive = path.getOrNull(2) == "archive"
            if (path.size == 2 || isArchive) {
                val repoMetadata = metadata(api)
                val type: String
                val ref: String
                if (isArchive) {
                    require(path.getOrNull(3) == "refs" && path.getOrNull(4) in listOf("heads", "tags")) { "Use an explicit GitHub branch or tag archive" }
                    type = path[4]
                    ref = path.drop(5).joinToString("/").removeSuffix(".zip")
                } else {
                    type = "heads"
                    ref = repoMetadata.string("default_branch")
                }
                require(ref.isNotBlank()) { "Repository revision is missing" }
                val commit = metadata("$api/commits/${encode("refs/$type/$ref")}").string("sha")
                require(commit.matches(Regex("[a-f0-9]{40}"))) { "Invalid repository commit" }
                return ResolvedSource(
                    downloadUrl = "https://codeload.github.com/$owner/$repo/zip/$commit",
                    repository = repository, revision = commit,
                    tracking = if (type == "heads") "branch:$ref" else "tag:$ref",
                    source = true, filename = "$repo.zip", repositoryId = repoMetadata.string("id")
                )
            }
        }
        val filename = uri.path.substringAfterLast('/').ifBlank { "download.zip" }
        return ResolvedSource(downloadUrl = uri.toString(), filename = filename)
    }
}

internal fun JsonObject.string(name: String): String = this[name]?.jsonPrimitive?.content.orEmpty()

internal object SafeSourceZip {
    const val MAX_EXPANDED = 256L * 1024 * 1024

    suspend fun extract(archive: File, destination: File, maximum: Long = MAX_EXPANDED): List<String> {
        check(!destination.exists() || destination.listFiles().isNullOrEmpty()) { "Workspace must be empty" }
        destination.mkdirs()
        val root = destination.canonicalPath + File.separator
        var total = 0L
        val seen = mutableSetOf<String>()
        ZipFile(archive).use { zip ->
            val entries = zip.entries().asSequence().toList()
            require(entries.size in 1..20_000) { "Archive has too many files or is empty" }
            for (entry in entries) {
                currentCoroutineContext().ensureActive()
                require(entry.name.length <= 2048 && !entry.name.startsWith('/') &&
                    entry.name.none { it == '\\' || it == '\u0000' || it == ':' } &&
                    entry.name.split('/').none { it == ".." }) { "Unsafe archive path" }
                val file = File(destination, entry.name)
                require(file.canonicalPath.startsWith(root)) { "Archive path leaves workspace" }
                require(seen.add(file.canonicalPath)) { "Duplicate archive entry" }
                if (entry.isDirectory) {
                    file.mkdirs()
                } else {
                    file.parentFile?.mkdirs()
                    zip.getInputStream(entry).use { input ->
                        file.outputStream().use { output ->
                            val buffer = ByteArray(32 * 1024)
                            var length = 0L
                            while (true) {
                                currentCoroutineContext().ensureActive()
                                val count = input.read(buffer)
                                if (count < 0) break
                                length += count
                                total += count
                                require(total <= maximum && length <= 128L * 1024 * 1024) { "Archive expands beyond limits" }
                                output.write(buffer, 0, count)
                            }
                        }
                    }
                }
            }
        }
        // Files are always materialized as ordinary files, never links or executables.
        return destination.walkTopDown().filter { it.isFile }.mapNotNull {
            when {
                it.name == "application.fam" -> "project:" + it.parentFile.relativeTo(destination).invariantSeparatorsPath
                it.extension.equals("fap", true) -> "fap:" + it.relativeTo(destination).invariantSeparatorsPath
                else -> null
            }
        }.sorted().toList()
    }
}
