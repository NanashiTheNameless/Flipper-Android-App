package dev.namelessnanashi.flipperzero.faphub.category.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FapHubHiddenItem
import dev.namelessnanashi.flipperzero.core.pager.OffsetAndLimitPagingSource
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.SortType
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

internal const val FAPS_PAGE_SIZE = 10

class FapsCategoryPagingSource(
    private val fapNetworkApi: FapNetworkApi,
    private val fapCategory: FapCategory,
    private val sortType: SortType,
    private val target: FlipperTarget,
    private val hiddenItems: Set<FapHubHiddenItem>
) : OffsetAndLimitPagingSource<FapItemShort>(FAPS_PAGE_SIZE) {
    override val TAG = "FapsCategoryPagingSource"

    override suspend fun load(offset: Int, limit: Int): List<FapItemShort> {
        return fapNetworkApi.getAllItem(
            target = target,
            category = fapCategory,
            sortType = sortType,
            offset = offset,
            limit = limit
        ).getOrThrow().filterNot { hiddenItems.contains(it.id) }
    }
}
