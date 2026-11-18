package dev.namelessnanashi.flipperzero.faphub.installation.all.impl.api

import dev.namelessnanashi.flipperzero.bridge.dao.api.FapHubHideItemApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapBuildState
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.SortType
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapInstallationAllApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

private const val CHUNK_RECEIVED_COUNT = 50
private val INSTALLABLE_BUILD_STATES = setOf(FapBuildState.READY, FapBuildState.READY_ON_RELEASE)

@ContributesBinding(AppGraph::class, binding<FapInstallationAllApi>())
class FapInstallationAllImpl @Inject constructor(
    private val fapNetworkApi: FapNetworkApi,
    private val queueApi: FapInstallationQueueApi,
    private val manifestApi: FapManifestApi,
    private val targetProviderApi: FlipperTargetProviderApi,
    private val hideItemApi: FapHubHideItemApi
) : FapInstallationAllApi, LogTagProvider {
    override val TAG = "FapInstallationAllApi"

    override suspend fun installAll(): Int {
        info { "Start receive installed apps" }
        val alreadyInstalledApps = manifestApi.getManifestFlow()
            .filter { it is FapManifestState.Loaded && !it.inProgress }
            .mapNotNull { state ->
                (state as? FapManifestState.Loaded)?.items
            }.first()
        info { "Received installed ${alreadyInstalledApps.size} apps" }
        val target = targetProviderApi.getFlipperTarget().filterNotNull().first()
        info { "Received target $target" }
        val allApps = mutableListOf<FapItemShort>()
        var currentOffset = 0
        var receivedAppsCount: Int
        do {
            val receivedApps = fapNetworkApi.getAllItem(
                target = target,
                sortType = SortType.UPDATE_AT_DESC,
                offset = currentOffset,
                limit = CHUNK_RECEIVED_COUNT
            ).getOrThrow()
            info { "Receive ${receivedApps.size} from network, with $currentOffset offset" }
            receivedAppsCount = receivedApps.size
            currentOffset += CHUNK_RECEIVED_COUNT
            allApps.addAll(receivedApps)
        } while (receivedAppsCount == CHUNK_RECEIVED_COUNT)
        info { "Received total ${allApps.size} apps from network" }
        val installedUids = alreadyInstalledApps.map { it.uid }.toSet()
        val hiddenUids = hideItemApi.getHiddenItems().first()
        // Only apps that are not installed yet, not hidden by the user, and can install on
        // this Flipper. Installed apps are left alone; outdated ones belong to Update-All.
        val appsToInstall = allApps
            .filterNot { installedUids.contains(it.id) }
            .filterNot { hiddenUids.contains(it.id) }
            .filter { it.upToDateVersion.buildState in INSTALLABLE_BUILD_STATES }
        info { "Apps to install: ${appsToInstall.size} apps" }
        appsToInstall.forEach { appItem ->
            queueApi.enqueue(
                FapActionRequest.Install(
                    applicationAlias = appItem.applicationAlias,
                    applicationUid = appItem.id,
                    applicationName = appItem.name,
                    toVersion = appItem.upToDateVersion,
                    categoryAlias = appItem.category.name,
                    iconUrl = appItem.picUrl
                )
            )
        }
        info { "Enqueue done!" }
        return appsToInstall.size
    }
}
