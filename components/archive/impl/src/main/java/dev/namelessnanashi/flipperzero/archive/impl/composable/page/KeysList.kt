package dev.namelessnanashi.flipperzero.archive.impl.composable.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.items
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.archive.impl.R
import dev.namelessnanashi.flipperzero.archive.impl.composable.key.ComposableKeySmall
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

private const val GRID_WIDTH = 2
private const val GRID_ROW_WEIGHT = 1f / GRID_WIDTH

@Suppress("FunctionName")
fun LazyListScope.ComposableKeysGrid(
    keys: List<FlipperKey>,
    synchronizationUiApi: SynchronizationUiApi,
    synchronizationState: SynchronizationState,
    onKeyOpen: (FlipperKeyPath) -> Unit
) {
    items(keys.windowed(GRID_WIDTH, GRID_WIDTH, partialWindows = true)) { items ->
        Row(
            modifier = Modifier
                .padding(horizontal = 7.dp)
                .fillMaxWidth()
        ) {
            items.forEach {
                ComposableKeySmall(
                    modifier = Modifier.weight(GRID_ROW_WEIGHT),
                    synchronizationContent = {
                        synchronizationUiApi.RenderSynchronizationState(
                            it.synchronized,
                            synchronizationState,
                            withText = false
                        )
                    },
                    keyPath = it.getKeyPath(),
                    onOpenKey = { onKeyOpen(it.getKeyPath()) }
                )
            }
            repeat(GRID_WIDTH - items.size) {
                Box(Modifier.weight(GRID_ROW_WEIGHT))
            }
        }
    }
}

@Composable
fun ComposableFavoriteKeysTitle(
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.padding(top = 24.dp, start = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = stringResource(R.string.archive_tab_general_favorite_title),
            style = LocalTypography.current.buttonB16
        )
        Icon(
            modifier = Modifier
                .padding(horizontal = 6.dp)
                .size(size = 20.dp),
            painter = painterResource(DesignSystem.drawable.ic_star_enabled),
            tint = LocalPallet.current.keyFavorite,
            contentDescription = null
        )
    }
}

@Composable
fun ComposableAllKeysTitle(
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier.padding(top = 24.dp, start = 14.dp),
        text = stringResource(R.string.archive_tab_general_all_title),
        style = LocalTypography.current.buttonB16
    )
}

@Suppress("FunctionName")
fun LazyListScope.ComposableFavoritePathsGrid(
    paths: List<FlipperKeyPath>,
    knownKeys: List<FlipperKey>,
    synchronizationUiApi: SynchronizationUiApi,
    synchronizationState: SynchronizationState,
    onKeyOpen: (FlipperKeyPath) -> Unit,
    onManageFavorites: () -> Unit
) {
    val known = knownKeys.associateBy { it.path.pathToKey }
    items(paths.windowed(GRID_WIDTH, GRID_WIDTH, partialWindows = true)) { row ->
        Row(Modifier.padding(horizontal = 7.dp).fillMaxWidth()) {
            row.forEach { path ->
                val key = known[path.path.pathToKey]
                ComposableKeySmall(
                    modifier = Modifier.weight(GRID_ROW_WEIGHT),
                    keyPath = path,
                    synchronizationContent = {
                        if (key != null) synchronizationUiApi.RenderSynchronizationState(key.synchronized, synchronizationState, false)
                        else Text(path.path.extension.uppercase().takeIf { it.length <= 8 }.orEmpty(), style = LocalTypography.current.subtitleM10, color = LocalPallet.current.text60)
                    },
                    onOpenKey = { if (key != null) onKeyOpen(path) else onManageFavorites() }
                )
            }
            repeat(GRID_WIDTH - row.size) { Box(Modifier.weight(GRID_ROW_WEIGHT)) }
        }
    }
}
