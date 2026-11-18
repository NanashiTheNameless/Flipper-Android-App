package dev.namelessnanashi.flipperzero.bridge.dao.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FapHubHiddenItem
import kotlinx.coroutines.flow.Flow

interface FapHubHideItemApi {

    fun getHiddenItems(): Flow<Set<FapHubHiddenItem>>
    suspend fun isHidden(applicationUid: String): Boolean
    suspend fun hideItem(applicationUid: String)
    suspend fun unHideItem(applicationUid: String)
}
