package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.provider.Settings
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material.AlertDialog
import androidx.compose.material.Button as MaterialButton
import androidx.compose.material.ButtonDefaults
import androidx.compose.material.Card
import androidx.compose.material.Checkbox
import androidx.compose.material.LinearProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.material.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import java.io.File

@Composable
internal fun SourceScreen(
    jobs: SourceJobs, backup: SourceBackup, initialUrl: String, initialProject: String,
    incomingFile: Uri?, inputError: String, onClose: () -> Unit
) {
    val context = LocalContext.current
    val records by jobs.store.records.collectAsState()
    val preferences by jobs.store.preferences.collectAsState()
    val widgetPresets by jobs.store.widgetPresets.collectAsState()
    var url by remember(initialUrl) { mutableStateOf(initialUrl) }
    var sdkUrl by remember(preferences.sdkUrl) { mutableStateOf(preferences.sdkUrl) }
    var setupOpen by remember { mutableStateOf(false) }
    var pendingImport by remember { mutableStateOf<Uri?>(null) }
    var restoreApps by remember { mutableStateOf(true) }
    var localError by remember { mutableStateOf("") }
    val pickApp = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri -> if (uri != null) jobs.importFile(uri) }
    val exportBackup = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/zip")) { uri -> if (uri != null) backup.export(uri) }
    val importBackup = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { pendingImport = it }
    val permission = rememberLauncherForActivityResult(ActivityResultContracts.RequestPermission()) { allowed ->
        localError = if (allowed) "Termux permission granted" else "Allow Run commands in Termux under this app’s permissions in Android Settings."
    }
    fun openTermux() {
        val intent = context.packageManager.getLaunchIntentForPackage("com.termux")
        if (intent != null) context.startActivity(intent) else context.startActivity(Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/termux/termux-app/releases")))
    }
    Surface(Modifier.fillMaxSize().systemBarsPadding().imePadding(), color = LocalPallet.current.background) {
        LazyColumn(Modifier.fillMaxSize().padding(horizontal = 16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            item {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    TextButton(onClick = onClose) { Text("Back") }
                    Text(stringResource(R.string.source_title), style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
                }
            }
            item {
                Text("Paste a repository, release, source ZIP, or .fap link. Builds run on this phone using Termux; Flipper-Nanashi handles installation.",
                    style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60)
            }
            item {
                OutlinedTextField(url, { url = it }, label = { Text("App URL") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                Row {
                    Button(onClick = { jobs.resolve(url, initialProject) }, enabled = url.isNotBlank()) { Text("Inspect link") }
                    TextButton(onClick = { pickApp.launch(arrayOf("*/*")) }) { Text("Choose app or ZIP") }
                }
                if (incomingFile != null) Button(onClick = { jobs.importFile(incomingFile) }) { Text("Inspect shared file") }
                if (inputError.isNotBlank()) Text(inputError, color = LocalPallet.current.text100)
                if (localError.isNotBlank()) Text(localError, color = LocalPallet.current.text100)
            }
            item {
                TextButton(onClick = { setupOpen = !setupOpen }) { Text("Builder setup${if (setupOpen) " ▴" else " ▾"}") }
                if (setupOpen) {
                    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                        Text("ARM64 phones: install Termux, paste the setup command once, then grant Flipper-Nanashi permission to run commands in Termux. Future builds stay here.", color = LocalPallet.current.text60)
                        Row {
                            TextButton(onClick = ::openTermux) { Text("Open / install Termux") }
                            TextButton(onClick = {
                                context.getSystemService(ClipboardManager::class.java).setPrimaryClip(ClipData.newPlainText("Builder setup", TermuxBackend.setup))
                                localError = "Setup command copied. Paste it in Termux once."
                            }) { Text("Copy setup") }
                        }
                        Row {
                            TextButton(onClick = { permission.launch(TermuxBackend.PERMISSION) }) { Text("Grant build access") }
                            TextButton(onClick = { context.startActivity(Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS, Uri.parse("package:${context.packageName}"))) }) { Text("App permissions") }
                        }
                        Button(onClick = { jobs.checkBuilder() }) { Text("Check builder setup") }
                        OutlinedTextField(sdkUrl, { sdkUrl = it }, label = { Text("SDK ZIP URL override (optional)") }, modifier = Modifier.fillMaxWidth())
                        Text("By default, the Momentum SDK is matched to the exact release or development revision on your Flipper. Leave this blank for automatic selection.", color = LocalPallet.current.text60)
                        Button(onClick = {
                            runCatching { if (sdkUrl.isNotBlank()) SourceHttp.https(sdkUrl); jobs.store.savePreferences(preferences.copy(sdkUrl = sdkUrl.trim())) }
                                .onFailure { localError = it.message.orEmpty() }
                        }) { Text("Save SDK profile") }
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Checkbox(preferences.trustedBuilds, { jobs.store.savePreferences(preferences.copy(trustedBuilds = it)) })
                            Text("I trust the sources I choose to build. Build scripts run with Termux’s access.", color = LocalPallet.current.text100)
                        }
                    }
                }
            }
            item {
                Text("Import & export · Beta", style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
                Text("Back up keys, notes, settings, favorites, saved devices, hidden apps, widget presets, app metadata and build recipes. Binaries are included for apps without rebuildable sources. Connect your Flipper for a complete installed-app backup.", color = LocalPallet.current.text60)
                Row {
                    Button(onClick = { exportBackup.launch("Flipper-Nanashi-Backup.zip") }) { Text("Export backup") }
                    TextButton(onClick = { importBackup.launch(arrayOf("application/zip", "application/octet-stream")) }) { Text("Import backup") }
                }
            }
            if (widgetPresets.isNotEmpty()) {
                item {
                    Text("Imported widget presets", style = LocalTypography.current.buttonB16, color = LocalPallet.current.text100)
                    Text("Add a home-screen widget and select the matching restored key. Android requires widgets to be placed again on a new phone.", color = LocalPallet.current.text60)
                    widgetPresets.forEach { preset -> Text("${preset.key?.path?.nameWithoutExtension ?: "Unassigned"} · ${preset.type.lowercase().replace('_', ' ')}", color = LocalPallet.current.text100) }
                }
            }
            items(records.sortedByDescending { it.updatedAt }, key = { it.id }) { record ->
                SourceJobCard(record, jobs)
            }
        }
    }
    pendingImport?.let { uri ->
        AlertDialog(
            onDismissRequest = { pendingImport = null },
            title = { Text("Import backup") },
            text = {
                Column {
                    Text("Import replaces user settings and favorites, and updates matching keys. Existing keys outside the backup are kept. Source records are imported without running their build scripts.")
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Checkbox(restoreApps, { restoreApps = it })
                        Text("Also restore or rebuild apps on the connected Flipper")
                    }
                }
            },
            confirmButton = { TextButton(onClick = { pendingImport = null; backup.import(uri, restoreApps) }) { Text("Import") } },
            dismissButton = { TextButton(onClick = { pendingImport = null }) { Text("Cancel") } }
        )
    }
}

@Composable
private fun Button(onClick: () -> Unit, enabled: Boolean = true, content: @Composable RowScope.() -> Unit) {
    MaterialButton(onClick = onClick, enabled = enabled, colors = ButtonDefaults.buttonColors(
        backgroundColor = LocalPallet.current.content, contentColor = LocalPallet.current.onContent
    ), content = content)
}

@Composable
private fun SourceJobCard(record: SourceRecord, jobs: SourceJobs) {
    val context = LocalContext.current
    var category by remember(record.id, record.category) { mutableStateOf(record.category) }
    var filename by remember(record.id, record.appId) {
        mutableStateOf(if (record.installedPath.isNotBlank()) File(record.installedPath).nameWithoutExtension else record.appId)
    }
    var showLog by remember { mutableStateOf(false) }
    Card(Modifier.fillMaxWidth()) {
        Column(Modifier.padding(14.dp), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            Text(record.name.ifBlank { record.inputUrl }, style = LocalTypography.current.buttonB16, color = LocalPallet.current.text100)
            if (record.name.isNotBlank()) SelectionContainer { Text(record.inputUrl, color = LocalPallet.current.text60) }
            if (record.tracking != "file") Text("Source: ${record.tracking}", color = LocalPallet.current.text60)
            if (record.project.isNotBlank()) Text("Project: ${record.project}", color = LocalPallet.current.text60)
            Text(record.phase.replace('_', ' ').lowercase().replaceFirstChar { it.uppercase() }, color = LocalPallet.current.text60)
            if (record.isBusy) {
                LinearProgressIndicator(Modifier.fillMaxWidth(), color = LocalPallet.current.text100)
                TextButton(onClick = { jobs.cancel(record.id) }) { Text("Cancel") }
            }
            if (record.message.isNotBlank()) Text(record.message, color = LocalPallet.current.text100)
            if (record.revision.isNotBlank()) SelectionContainer { Text("Revision: ${record.revision}", color = LocalPallet.current.text60) }
            if (record.api.isNotBlank()) Text("API ${record.api}", color = LocalPallet.current.text60)
            if (record.installedPath.isNotBlank()) Text(record.installedPath, color = LocalPallet.current.text60)
            if (record.phase == "CHOOSE") {
                LazyColumn(Modifier.heightIn(max = 300.dp)) {
                    items(record.choices, key = { it }) { choice ->
                        TextButton(onClick = { jobs.choose(record.id, choice) }) { Text(choice.substringAfter(':').ifBlank { "Root application" }) }
                    }
                }
            }
            if (!record.isBusy && record.appId.isNotBlank()) {
                OutlinedTextField(category, { category = it }, label = { Text("Flipper app category") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                OutlinedTextField(filename, { filename = it }, label = { Text("Filename (without .fap)") }, singleLine = true, modifier = Modifier.fillMaxWidth())
                if (record.sourceProject) {
                    if (record.phase in listOf("FAILED", "CANCELLED", "INTERRUPTED", "READY") && File(jobs.store.directory(record.id), "artifact.fap").isFile) {
                        Button(onClick = { jobs.install(record.id, category, filename) }) { Text("Install built app") }
                    }
                    Button(onClick = { jobs.buildAndInstall(record.id, category, filename) }) { Text("Build & Install") }
                }
                else if (File(jobs.store.directory(record.id), "artifact.fap").isFile) Button(onClick = { jobs.install(record.id, category, filename) }) { Text("Install app") }
                if (record.inputUrl.startsWith("https://") && record.tracking != "catalog") {
                    Row {
                        TextButton(onClick = { jobs.checkUpdates(record.id) }) { Text("Check for updates") }
                        TextButton(onClick = {
                            val share = Intent(Intent.ACTION_SEND).setType("text/plain").putExtra(Intent.EXTRA_TEXT, SourceLinks.create(record))
                            context.startActivity(Intent.createChooser(share, "Share app link"))
                        }) { Text("Share link") }
                    }
                }
                if (record.updateAvailable) Button(onClick = { jobs.prepareUpdate(record.id) }) { Text("Review update") }
            }
            if (!record.isBusy && record.phase in listOf("FAILED", "CANCELLED", "INTERRUPTED") && record.resolvedUrl.isNotBlank()) {
                TextButton(onClick = { jobs.retry(record.id) }) { Text("Download again") }
            }
            if (record.log.isNotBlank()) {
                TextButton(onClick = { showLog = !showLog }) { Text(if (showLog) "Hide build log" else "Show build log") }
                if (showLog) SelectionContainer { Text(record.log, style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60) }
            }
        }
    }
}
