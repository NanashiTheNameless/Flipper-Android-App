package dev.namelessnanashi.flipperzero.faphub.search.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.paging.LoadState
import androidx.paging.compose.LazyPagingItems
import dev.namelessnanashi.flipperzero.core.ui.searchbar.ComposableSearchBar
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.paging.ComposableFapsList
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapErrorSize
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.search.impl.R
import dev.namelessnanashi.flipperzero.faphub.search.impl.model.RequestTooSmallException

@Composable
fun ComposableSearchScreen(
    onBack: () -> Unit,
    onFapItemClick: (FapItemShort) -> Unit,
    onChangeText: (text: String) -> Unit,
    errorsRenderer: FapHubComposableErrorsRenderer,
    fapsList: LazyPagingItems<FapItemShort>,
    searchRequest: String,
    modifier: Modifier = Modifier,
    installationButton: @Composable (FapItemShort?, Modifier) -> Unit
) {
    Column(modifier = modifier) {
        ComposableSearchBar(
            hint = stringResource(R.string.faphub_search_hint),
            onChangeText = onChangeText,
            onBack = onBack
        )
        LazyColumn {
            if (fapsList.loadState.append.endOfPaginationReached && fapsList.itemCount == 0) {
                item {
                    ComposableResultEmpty(
                        request = searchRequest,
                        modifier = Modifier.fillParentMaxSize()
                    )
                }
            } else {
                val loadError = (fapsList.loadState.refresh as? LoadState.Error)?.error
                when (loadError) {
                    is RequestTooSmallException -> item {
                        ComposableSearchRequestTooSmall(Modifier.fillParentMaxSize())
                    }

                    else -> ComposableFapsList(
                        faps = fapsList,
                        onOpenFapItem = onFapItemClick,
                        installationButton = installationButton,
                        errorsRenderer = errorsRenderer,
                        defaultFapErrorSize = FapErrorSize.FULLSCREEN
                    )
                }
            }
        }
    }
}
