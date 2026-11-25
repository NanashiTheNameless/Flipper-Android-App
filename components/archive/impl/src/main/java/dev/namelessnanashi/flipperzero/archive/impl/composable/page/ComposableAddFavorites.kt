package dev.namelessnanashi.flipperzero.archive.impl.composable.page

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Checkbox
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Surface
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import dev.namelessnanashi.flipperzero.archive.impl.R
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteApp
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteSaveState
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
fun ComposableAddFavorites(
    favorites: List<FlipperKeyPath>,
    keys: List<FlipperKey>,
    apps: List<FavoriteApp>,
    appsLoading: Boolean,
    syncing: Boolean,
    saveState: FavoriteSaveState,
    onSave: (List<FlipperKeyPath>) -> Unit,
    onDismiss: () -> Unit
) {
    var draft by remember { mutableStateOf(favorites) }
    var query by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    val saving = saveState == FavoriteSaveState.SAVING
    val enabled = !saving && !syncing
    val appPaths = apps.map { it.keyPath }.toSet()
    val otherPaths = (favorites + keys.map { it.getKeyPath() }).distinct().filter { it !in appPaths }
    val matchingApps = apps.filter {
        it.name.contains(query, ignoreCase = true) || it.keyPath.path.pathToKey.contains(query, ignoreCase = true)
    }
    val matchingPaths = (otherPaths + draft.filter { it !in appPaths }).distinct()
        .filter { it.path.pathToKey.contains(query, ignoreCase = true) }

    fun toggle(path: FlipperKeyPath) {
        draft = if (path in draft) draft - path else draft + path
    }

    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(24.dp), color = LocalPallet.current.background) {
            Column(
                Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(stringResource(R.string.archive_add_favorites), style = LocalTypography.current.titleB18, color = LocalPallet.current.text100)
                Text(
                    stringResource(if (syncing) R.string.archive_favorites_syncing else R.string.archive_favorites_hint),
                    style = LocalTypography.current.bodyM14,
                    color = LocalPallet.current.text60
                )
                OutlinedTextField(
                    value = query,
                    onValueChange = { query = it },
                    label = { Text(stringResource(R.string.archive_favorites_search)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    item { FavoriteGroupTitle(stringResource(R.string.archive_favorites_apps)) }
                    if (appsLoading) {
                        item { Text(stringResource(R.string.archive_favorites_apps_loading), color = LocalPallet.current.text60) }
                    }
                    items(matchingApps, key = { "app:${it.keyPath.path.pathToKey}" }) { app ->
                        FavoriteChoice(app.name, app.keyPath, app.keyPath in draft, enabled) { toggle(app.keyPath) }
                    }
                    item { FavoriteGroupTitle(stringResource(R.string.archive_favorites_files)) }
                    items(matchingPaths, key = { "file:${it.path.pathToKey}" }) { path ->
                        FavoriteChoice(path.path.nameWithoutExtension, path, path in draft, enabled) { toggle(path) }
                    }
                    if (matchingApps.isEmpty() && matchingPaths.isEmpty()) {
                        item { Text(stringResource(R.string.archive_favorites_no_matches), color = LocalPallet.current.text60) }
                    }
                    item {
                        OutlinedTextField(
                            value = filePath,
                            onValueChange = { filePath = it },
                            label = { Text(stringResource(R.string.archive_favorites_path)) },
                            placeholder = { Text("/ext/subghz/remote/BedRoom.txt") },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    item {
                        ComposableCancelButton(
                            stringResource(R.string.archive_favorites_add_path),
                            {
                                val path = FlipperKeyPath(FlipperFilePath.fromFavoritePath(filePath.trim()), false)
                                draft = (draft + path).distinct()
                                filePath = ""
                            },
                            enabled = enabled && filePath.isNotBlank() && filePath.none { it == '\n' || it == '\r' || it == '\u0000' }
                        )
                    }
                }
                if (saveState == FavoriteSaveState.ERROR) {
                    Text(stringResource(R.string.archive_favorites_error), color = LocalPallet.current.text100)
                }
                if (saving) CircularProgressIndicator(color = LocalPallet.current.text100)
                ComposableCancelButton(stringResource(R.string.archive_favorites_save), { onSave(draft) }, enabled = enabled)
                ComposableCancelButton(stringResource(R.string.archive_favorites_cancel), onDismiss, enabled = !saving)
            }
        }
    }
}

@Composable
private fun FavoriteGroupTitle(title: String) {
    Text(title, style = LocalTypography.current.buttonB16, color = LocalPallet.current.text100)
}

@Composable
private fun FavoriteChoice(name: String, path: FlipperKeyPath, selected: Boolean, enabled: Boolean, onToggle: () -> Unit) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Column(Modifier.weight(1f)) {
            Text(name, color = LocalPallet.current.text100)
            Text(path.path.pathToKey, style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60)
        }
        val description = stringResource(
            if (selected) R.string.archive_favorites_remove else R.string.archive_favorites_add_key,
            name
        )
        Checkbox(selected, { onToggle() }, enabled = enabled, modifier = Modifier.semantics { contentDescription = description })
    }
}
