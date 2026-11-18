package dev.namelessnanashi.flipperzero.faphub.dao.api

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.SortType
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

interface FapNetworkApi {
    suspend fun getHostUrl(): String
    suspend fun getFeaturedItem(
        target: FlipperTarget
    ): Result<FapItemShort>

    @Suppress("LongParameterList")
    suspend fun getAllItem(
        target: FlipperTarget,
        category: FapCategory? = null,
        sortType: SortType,
        offset: Int,
        limit: Int,
        applicationIds: List<String>? = null
    ): Result<List<FapItemShort>>

    suspend fun search(
        target: FlipperTarget,
        query: String,
        offset: Int,
        limit: Int
    ): Result<List<FapItemShort>>

    suspend fun getCategories(target: FlipperTarget): Result<List<FapCategory>>
    suspend fun getFapItemById(target: FlipperTarget, id: String): Result<FapItem>
}
