package dev.namelessnanashi.flipperzero.faphub.search.impl.viewmodel

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FapHubHiddenItem
import dev.namelessnanashi.flipperzero.core.pager.OffsetAndLimitPagingSource
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.search.impl.model.RequestTooSmallException
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

internal const val FAPS_PAGE_SIZE = 10

class FapsSearchPagingSource(
    private val fapNetworkApi: FapNetworkApi,
    private val searchRequest: String,
    private val target: FlipperTarget,
    private val hiddenItems: Set<FapHubHiddenItem>
) : OffsetAndLimitPagingSource<FapItemShort>(FAPS_PAGE_SIZE) {
    override val TAG = "FapsSearchPagingSource"
    override suspend fun load(offset: Int, limit: Int): List<FapItemShort> {
        if (searchRequest.isNotBlank() && searchRequest.length < 2) {
            throw RequestTooSmallException()
        }

        return fapNetworkApi.search(
            target = target,
            query = searchRequest,
            offset = offset,
            limit = limit
        ).getOrThrow().filterNot { hiddenItems.contains(it.id) }
    }
}
