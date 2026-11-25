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
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Divider
import androidx.compose.material.IconButton
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
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteSaveState
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
fun ComposableFavoritesEditor(
    favorites: List<FlipperKeyPath>,
    otherKeys: List<FlipperKey>,
    syncing: Boolean,
    saveState: FavoriteSaveState,
    onSave: (List<FlipperKeyPath>) -> Unit,
    onDismiss: () -> Unit
) {
    // Keep edits private until Save; Cancel never changes the database or the Flipper.
    var draft by remember { mutableStateOf(favorites) }
    var query by remember { mutableStateOf("") }
    var filePath by remember { mutableStateOf("") }
    val saving = saveState == FavoriteSaveState.SAVING
    val enabled = !saving && !syncing
    val available = (favorites + otherKeys.map { it.getKeyPath() }).distinct()
        .filter { it !in draft && it.path.pathToKey.contains(query, ignoreCase = true) }
    Dialog(onDismissRequest = { if (!saving) onDismiss() }) {
        Surface(shape = RoundedCornerShape(24.dp), color = LocalPallet.current.background) {
            Column(
                modifier = Modifier.fillMaxWidth().fillMaxHeight(0.9f).padding(20.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                Text(
                    stringResource(R.string.archive_edit_favorites),
                    style = LocalTypography.current.titleB18,
                    color = LocalPallet.current.text100
                )
                Text(
                    stringResource(if (syncing) R.string.archive_favorites_syncing else R.string.archive_favorites_hint),
                    style = LocalTypography.current.bodyM14,
                    color = LocalPallet.current.text60
                )
                LazyColumn(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    if (draft.isEmpty()) {
                        item { Text(stringResource(R.string.archive_favorites_empty), color = LocalPallet.current.text60) }
                    }
                    items(draft, key = { it.path.pathToKey }) { path ->
                        val index = draft.indexOf(path)
                        Column {
                            Text("${index + 1}. ${path.path.nameWithoutExtension}", color = LocalPallet.current.text100)
                            Text(path.path.pathToKey, style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60)
                            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
                                FavoriteAction("↑", stringResource(R.string.archive_favorites_move_up, path.path.nameWithoutExtension), enabled && index > 0) {
                                    draft = draft.toMutableList().apply { add(index - 1, removeAt(index)) }
                                }
                                FavoriteAction("↓", stringResource(R.string.archive_favorites_move_down, path.path.nameWithoutExtension), enabled && index < draft.lastIndex) {
                                    draft = draft.toMutableList().apply { add(index + 1, removeAt(index)) }
                                }
                                FavoriteAction("×", stringResource(R.string.archive_favorites_remove, path.path.nameWithoutExtension), enabled) {
                                    draft = draft - path
                                }
                            }
                            Divider(color = LocalPallet.current.text30)
                        }
                    }
                    item {
                        Text(stringResource(R.string.archive_favorites_add), style = LocalTypography.current.buttonB16, color = LocalPallet.current.text100)
                    }
                    item {
                        OutlinedTextField(
                            value = query,
                            onValueChange = { query = it },
                            label = { Text(stringResource(R.string.archive_favorites_search)) },
                            singleLine = true,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                    if (available.isEmpty()) {
                        item { Text(stringResource(R.string.archive_favorites_no_matches), color = LocalPallet.current.text60) }
                    }
                    items(available, key = { "add:${it.path.pathToKey}" }) { path ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Column(Modifier.weight(1f)) {
                                Text(path.path.nameWithoutExtension, color = LocalPallet.current.text100)
                                Text(path.path.pathToKey, style = LocalTypography.current.bodyM14, color = LocalPallet.current.text60)
                            }
                            FavoriteAction("+", stringResource(R.string.archive_favorites_add_key, path.path.nameWithoutExtension), enabled) {
                                draft = draft + path
                            }
                        }
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
private fun FavoriteAction(symbol: String, description: String, enabled: Boolean, onClick: () -> Unit) {
    IconButton(onClick, enabled = enabled, modifier = Modifier.semantics { contentDescription = description }) {
        Text(symbol, color = if (enabled) LocalPallet.current.text100 else LocalPallet.current.text30)
    }
}
