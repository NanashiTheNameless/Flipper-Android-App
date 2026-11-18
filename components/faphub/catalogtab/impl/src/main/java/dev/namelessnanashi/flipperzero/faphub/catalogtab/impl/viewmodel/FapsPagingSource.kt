package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FapHubHiddenItem
import dev.namelessnanashi.flipperzero.core.pager.OffsetAndLimitPagingSource
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.SortType
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

internal const val FAPS_PAGE_SIZE = 10

class FapsPagingSource(
    private val fapNetworkApi: FapNetworkApi,
    private val sortType: SortType,
    private val target: FlipperTarget,
    private val hiddenItems: Set<FapHubHiddenItem>
) : OffsetAndLimitPagingSource<FapItemShort>(FAPS_PAGE_SIZE) {
    override val TAG = "FapsPagingSource"
    override suspend fun load(offset: Int, limit: Int): List<FapItemShort> {
        return fapNetworkApi.getAllItem(
            sortType = sortType,
            offset = offset,
            limit = limit,
            target = target
        ).getOrThrow().filterNot { hiddenItems.contains(it.id) }
    }
}
