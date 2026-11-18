package dev.namelessnanashi.flipperzero.faphub.dao.network.api

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.pmap
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapVersionApi
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.api.FapNetworkVersionApi
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.model.requests.KtorfitDetailedVersionRequest
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val MAX_QUERY_ARRAY_SIZE = 500

@ContributesBinding(AppGraph::class, binding<FapVersionApi>())
class FapVersionApiImpl @Inject constructor(
    private val fapNetworkVersionApi: FapNetworkVersionApi
) : FapVersionApi, LogTagProvider {
    override val TAG = "FapVersionApi"

    override suspend fun getVersionsMap(versions: List<String>): Map<String, SemVer> {
        val fetchedVersions = versions.chunked(MAX_QUERY_ARRAY_SIZE)
            .pmap {
                fapNetworkVersionApi.getVersions(
                    KtorfitDetailedVersionRequest(
                        applicationVersions = it,
                        limit = it.size,
                        offset = 0
                    )
                )
            }.flatten()

        return fetchedVersions.associate {
            val numberVersion = SemVer.fromString(it.version)
                ?: error("Failed parse ${it.version}")
            it.id to numberVersion
        }
    }
}
