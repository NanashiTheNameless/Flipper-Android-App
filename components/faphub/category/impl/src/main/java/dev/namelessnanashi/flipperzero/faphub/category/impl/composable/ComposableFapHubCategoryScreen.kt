package dev.namelessnanashi.flipperzero.faphub.category.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBarWithIcon
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.SwipeRefresh
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.paging.ComposableFapsList
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.paging.ComposableSortChoice
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.SortType
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableFapHubCategory(
    onBack: () -> Unit,
    onOpenSearch: () -> Unit,
    onOpenFapItem: (FapItemShort) -> Unit,
    errorsRenderer: FapHubComposableErrorsRenderer,
    fapsList: LazyPagingItems<FapItemShort>,
    sortType: SortType,
    categoryName: String,
    onSelectSortType: (SortType) -> Unit,
    onlyNotInstalled: Boolean,
    onToggleOnlyNotInstalled: (Boolean) -> Unit,
    hiddenIds: Set<String>,
    modifier: Modifier = Modifier,
    installationButton: @Composable (FapItemShort?, Modifier) -> Unit
) {
    Column(modifier = modifier) {
        OrangeAppBarWithIcon(
            title = categoryName,
            onBack = onBack,
            endIconId = DesignSystem.drawable.ic_search,
            onEndClick = onOpenSearch
        )
        SwipeRefresh(onRefresh = fapsList::refresh) {
            LazyColumn {
                if (fapsList.loadState.refresh !is LoadState.Error) {
                    item {
                        ComposableSortChoice(
                            title = null,
                            sortType = sortType,
                            onSelectSortType = onSelectSortType,
                            onlyNotInstalled = onlyNotInstalled,
                            onToggleOnlyNotInstalled = onToggleOnlyNotInstalled
                        )
                    }
                }

                ComposableFapsList(
                    faps = fapsList,
                    onOpenFapItem = onOpenFapItem,
                    installationButton = installationButton,
                    errorsRenderer = errorsRenderer,
                    defaultFapErrorSize = FapErrorSize.FULLSCREEN,
                    hiddenIds = hiddenIds
                )
            }
        }
    }
}
