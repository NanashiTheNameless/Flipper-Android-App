package dev.namelessnanashi.flipperzero.faphub.search.impl.viewmodel

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.cachedIn
import dev.namelessnanashi.flipperzero.bridge.dao.api.FapHubHideItemApi
import dev.namelessnanashi.flipperzero.core.pager.distinctBy
import dev.namelessnanashi.flipperzero.core.pager.loadingPagingDataFlow
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

class FapHubSearchViewModel @Inject constructor(
    private val fapNetworkApi: FapNetworkApi,
    fapHubHideItemApi: FapHubHideItemApi,
    targetProviderApi: FlipperTargetProviderApi
) : DecomposeViewModel() {
    private val searchRequestFlow = MutableStateFlow("")

    @OptIn(ExperimentalCoroutinesApi::class)
    val faps = combine(
        searchRequestFlow,
        targetProviderApi.getFlipperTarget(),
        fapHubHideItemApi.getHiddenItems()
    ) { searchRequest, target, hiddenItems ->
        if (target == null) {
            return@combine loadingPagingDataFlow()
        }
        Pager(PagingConfig(pageSize = FAPS_PAGE_SIZE)) {
            FapsSearchPagingSource(fapNetworkApi, searchRequest, target, hiddenItems)
        }.flow.distinctBy { it.id }
    }.flatMapLatest { it }.cachedIn(viewModelScope)

    fun getSearchRequest() = searchRequestFlow.asStateFlow()

    fun onChangeSearchText(text: String) {
        viewModelScope.launch {
            searchRequestFlow.emit(text)
        }
    }
}
