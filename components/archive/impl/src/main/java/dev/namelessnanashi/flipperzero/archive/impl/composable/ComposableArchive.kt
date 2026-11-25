package dev.namelessnanashi.flipperzero.archive.impl.composable

import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListScope
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.rotate
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.archive.impl.R
import dev.namelessnanashi.flipperzero.archive.impl.composable.category.ComposableCategoryCard
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableAllKeysTitle
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableFavoriteKeysTitle
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableFavoritesEditor
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableAddFavorites
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableFavoritePathsGrid
import dev.namelessnanashi.flipperzero.archive.impl.composable.page.ComposableKeysGrid
import dev.namelessnanashi.flipperzero.archive.impl.model.CategoryItem
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteSaveState
import dev.namelessnanashi.flipperzero.archive.impl.model.FavoriteApp
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationUiApi
import dev.namelessnanashi.flipperzero.core.ktx.jre.roundPercentToString
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBarWithIcon
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.SwipeRefresh
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import kotlinx.collections.immutable.ImmutableList

@Composable
fun ComposableArchive(
    synchronizationUiApi: SynchronizationUiApi,
    onOpenSearchScreen: () -> Unit,
    onOpenKeyScreen: (FlipperKeyPath) -> Unit,
    onOpenCategory: (CategoryType) -> Unit,
    keys: ImmutableList<FlipperKey>?,
    synchronizationState: SynchronizationState,
    favoriteKeys: ImmutableList<FlipperKey>,
    favoritePaths: ImmutableList<FlipperKeyPath>,
    favoriteApps: ImmutableList<FavoriteApp>,
    favoriteAppsLoading: Boolean,
    categories: ImmutableList<CategoryItem>,
    deletedCategory: CategoryItem,
    lazyListState: LazyListState,
    favoriteSaveState: FavoriteSaveState,
    onSaveFavorites: (List<FlipperKeyPath>, () -> Unit) -> Unit,
    onRefresh: () -> Unit
) {
    val isKeysPresented = favoritePaths.isNotEmpty() || favoriteKeys.isNotEmpty() || !keys.isNullOrEmpty()

    var showFavoritesEditor by remember { mutableStateOf(false) }
    var showAddFavorites by remember { mutableStateOf(false) }
    ComposableArchiveReady(
        synchronizationUiApi = synchronizationUiApi,
        keys = keys,
        favoriteKeys = favoriteKeys,
        favoritePaths = favoritePaths,
        onRefresh = onRefresh,
        synchronizationState = synchronizationState,
        isKeysPresented = isKeysPresented,
        onOpenKeyScreen = onOpenKeyScreen,
        onOpenSearchScreen = onOpenSearchScreen,
        onOpenCategory = onOpenCategory,
        categories = categories,
        deletedCategory = deletedCategory,
        lazyListState = lazyListState,
        onEditFavorites = { showFavoritesEditor = true },
        onAddFavorites = { showAddFavorites = true }
    )
    if (showFavoritesEditor) {
        ComposableFavoritesEditor(
            favorites = favoritePaths,
            syncing = synchronizationState is SynchronizationState.InProgress,
            saveState = favoriteSaveState,
            onSave = { paths -> onSaveFavorites(paths) { showFavoritesEditor = false } },
            onDismiss = { showFavoritesEditor = false }
        )
    }
    if (showAddFavorites) {
        ComposableAddFavorites(
            favorites = favoritePaths,
            keys = favoriteKeys + keys.orEmpty(),
            apps = favoriteApps,
            appsLoading = favoriteAppsLoading,
            syncing = synchronizationState is SynchronizationState.InProgress,
            saveState = favoriteSaveState,
            onSave = { paths -> onSaveFavorites(paths) { showAddFavorites = false } },
            onDismiss = { showAddFavorites = false }
        )
    }
}

@Composable
private fun ComposableArchiveReady(
    synchronizationUiApi: SynchronizationUiApi,
    keys: ImmutableList<FlipperKey>?,
    favoriteKeys: ImmutableList<FlipperKey>,
    favoritePaths: ImmutableList<FlipperKeyPath>,
    lazyListState: LazyListState,
    onRefresh: () -> Unit,
    synchronizationState: SynchronizationState,
    isKeysPresented: Boolean,
    categories: ImmutableList<CategoryItem>,
    deletedCategory: CategoryItem,
    onOpenKeyScreen: (FlipperKeyPath) -> Unit,
    onOpenSearchScreen: () -> Unit,
    onOpenCategory: (CategoryType) -> Unit,
    onEditFavorites: () -> Unit,
    onAddFavorites: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.Top
    ) {
        OrangeAppBarWithIcon(
            title = stringResource(R.string.archive_title),
            endIconId = DesignSystem.drawable.ic_search,
            onEndClick = onOpenSearchScreen
        )
        if (synchronizationState is SynchronizationState.InProgress) {
            Row(
                modifier = Modifier.fillMaxWidth()
                    .clickableRipple(onClick = synchronizationUiApi::showSynchronizationDialog)
                    .padding(16.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                CircularProgressIndicator(Modifier.size(20.dp), color = LocalPallet.current.text100, strokeWidth = 2.dp)
                Text(
                    text = stringResource(R.string.archive_sync_percent, synchronizationState.progress.roundPercentToString()),
                    style = LocalTypography.current.bodyM14,
                    color = LocalPallet.current.text100
                )
                Text(
                    text = stringResource(R.string.archive_sync_show_status),
                    style = LocalTypography.current.bodyM14,
                    color = LocalPallet.current.text60
                )
            }
        }
        SwipeRefresh(onRefresh = onRefresh) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth(),
                state = lazyListState
            ) {
                item {
                    ComposableCategoryCard(
                        onOpenCategory = onOpenCategory,
                        categories = categories,
                        deletedCategory = deletedCategory
                    )
                }
                item {
                    ComposableCancelButton(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        text = stringResource(R.string.archive_add_favorites),
                        onClick = onAddFavorites
                    )
                    ComposableCancelButton(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 12.dp),
                        text = stringResource(R.string.archive_edit_favorites),
                        onClick = onEditFavorites
                    )
                }
                if (isKeysPresented) {
                    KeyCatalog(
                        favoriteKeys = favoriteKeys,
                        favoritePaths = favoritePaths,
                        onManageFavorites = onAddFavorites,
                        otherKeys = keys,
                        synchronizationUiApi = synchronizationUiApi,
                        synchronizationState = synchronizationState,
                        onKeyOpen = onOpenKeyScreen
                    )
                }
            }
        }
        if (!isKeysPresented) {
            if (synchronizationState is SynchronizationState.InProgress) {
                ComposableProgress()
            } else {
                ComposableNoKeys()
            }
        }
    }
}

@Suppress("FunctionName")
private fun LazyListScope.KeyCatalog(
    favoriteKeys: List<FlipperKey>,
    favoritePaths: List<FlipperKeyPath>,
    onManageFavorites: () -> Unit,
    otherKeys: List<FlipperKey>?,
    synchronizationUiApi: SynchronizationUiApi,
    synchronizationState: SynchronizationState,
    onKeyOpen: (FlipperKeyPath) -> Unit
) {
    if (favoritePaths.isNotEmpty()) {
        item {
            ComposableFavoriteKeysTitle()
        }
        ComposableFavoritePathsGrid(
            paths = favoritePaths,
            knownKeys = favoriteKeys,
            synchronizationUiApi = synchronizationUiApi,
            synchronizationState = synchronizationState,
            onKeyOpen = onKeyOpen,
            onManageFavorites = onManageFavorites
        )
    }

    if (!otherKeys.isNullOrEmpty()) {
        item {
            ComposableAllKeysTitle()
        }
        ComposableKeysGrid(
            otherKeys,
            synchronizationUiApi,
            synchronizationState,
            onKeyOpen
        )
    }
}

@Composable
private fun ColumnScope.ComposableNoKeys() {
    Box(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxWidth(),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = stringResource(R.string.archive_content_empty),
            style = LocalTypography.current.bodyR16,
            color = LocalPallet.current.text40
        )
    }
}

@Composable
private fun ColumnScope.ComposableProgress() {
    Column(
        modifier = Modifier
            .weight(weight = 1f)
            .fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        val infiniteTransition = rememberInfiniteTransition()
        val angle by infiniteTransition.animateFloat(
            initialValue = 0F,
            targetValue = 360F,
            animationSpec = infiniteRepeatable(
                animation = tween(durationMillis = 2000, easing = LinearEasing)
            )
        )
        Icon(
            modifier = Modifier.rotate(angle),
            painter = painterResource(DesignSystem.drawable.ic_progress),
            tint = LocalPallet.current.accentSecond,
            contentDescription = stringResource(R.string.archive_sync_progress)
        )
        Text(
            modifier = Modifier.padding(top = 8.dp),
            text = stringResource(R.string.archive_sync_progress),
            style = LocalTypography.current.bodyR16,
            color = LocalPallet.current.text40
        )
    }
}
