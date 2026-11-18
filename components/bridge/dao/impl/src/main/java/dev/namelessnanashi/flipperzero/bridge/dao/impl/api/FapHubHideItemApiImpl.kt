package dev.namelessnanashi.flipperzero.bridge.dao.impl.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.FapHubHideItemApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FapHubHiddenItem
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.HideFapHubApp
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.HideFapHubAppDao
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapHubHideItemApi>())
class FapHubHideItemApiImpl @Inject constructor(
    private val fapHubAppDao: HideFapHubAppDao
) : FapHubHideItemApi {
    override fun getHiddenItems(): Flow<Set<FapHubHiddenItem>> {
        return fapHubAppDao.fetchAllHideFapHub()
            .map { list -> list.map { it.applicationUid }.toSet() }
    }

    override suspend fun isHidden(applicationUid: String): Boolean {
        return fapHubAppDao.getOneItem(applicationUid) != null
    }

    override suspend fun hideItem(applicationUid: String) {
        fapHubAppDao.insert(HideFapHubApp(applicationUid = applicationUid))
    }

    override suspend fun unHideItem(applicationUid: String) {
        val hiddenItem = fapHubAppDao.getOneItem(applicationUid) ?: return
        fapHubAppDao.delete(hiddenItem)
    }
}
