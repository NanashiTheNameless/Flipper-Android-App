package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.material.MaterialTheme
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet

class SourceActivity : ComponentActivity() {
    private var incomingUrl by mutableStateOf("")
    private var incomingProject by mutableStateOf("")
    private var incomingFile by mutableStateOf<Uri?>(null)
    private var inputError by mutableStateOf("")

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        accept(intent)
        val component = ComponentHolder.component<SourceComponent>()
        setContent {
            FlipperThemeInternal {
                val pallet = LocalPallet.current
                MaterialTheme(colors = MaterialTheme.colors.copy(primary = pallet.text100, secondary = pallet.text100,
                    primaryVariant = pallet.text60, secondaryVariant = pallet.text60,
                    onPrimary = pallet.background, onSecondary = pallet.background)) {
                SourceScreen(component.sourceJobs(), component.sourceBackup(), incomingUrl,
                    incomingProject, incomingFile, inputError, ::finish)
                }
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        accept(intent)
    }

    @Suppress("DEPRECATION")
    private fun accept(intent: Intent) {
        inputError = ""
        incomingFile = null
        incomingUrl = ""
        incomingProject = ""
        runCatching {
            if (intent.action == Intent.ACTION_VIEW && intent.data?.scheme == "flipper-nanashi") {
                val parsed = SourceLinks.parse(requireNotNull(intent.data).toString())
                incomingUrl = parsed.first
                incomingProject = parsed.second
            } else if (intent.action == Intent.ACTION_SEND) {
                incomingFile = intent.getParcelableExtra(Intent.EXTRA_STREAM)
                if (incomingFile == null) incomingUrl = intent.getStringExtra(Intent.EXTRA_TEXT).orEmpty().trim()
            }
        }.onFailure { inputError = it.message.orEmpty() }
    }
}

internal object SourceLinks {
    fun parse(value: String): Pair<String, String> {
        require(value.length <= 8192) { "App link is too long" }
        val uri = java.net.URI(value)
        require(uri.scheme == "flipper-nanashi" && uri.host == "app" && uri.userInfo == null && uri.port == -1) { "Invalid app link" }
        val fields = uri.rawQuery.orEmpty().split('&').filter { it.isNotEmpty() }.map {
            java.net.URLDecoder.decode(it.substringBefore('='), "UTF-8") to java.net.URLDecoder.decode(it.substringAfter('=', ""), "UTF-8")
        }
        require(fields.map { it.first }.distinct().size == fields.size && fields.all { it.first in listOf("url", "project") }) { "Unsupported or repeated app link parameter" }
        val url = fields.toMap()["url"] ?: error("App link is missing its source URL")
        SourceHttp.https(url)
        val project = fields.toMap()["project"].orEmpty()
        require(!project.startsWith('/') && project.split('/').none { it == ".." } && project.none { it == '\\' || it.isISOControl() }) { "Invalid app project path" }
        return url to project
    }

    fun create(record: SourceRecord): String {
        SourceHttp.https(record.inputUrl)
        fun encode(value: String) = java.net.URLEncoder.encode(value, "UTF-8")
        return "flipper-nanashi://app?url=${encode(record.inputUrl)}" + if (record.project.isBlank()) "" else "&project=${encode(record.project)}"
    }
}
