package dev.namelessnanashi.flipperzero.faphub.installation.all.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapNetworkApi
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairApi
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairReport
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.api.FapManifestApi
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestItem
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.FapManifestState
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.ensureActive
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairProgress
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairStage
import kotlinx.coroutines.flow.filter
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.mapNotNull
import okio.buffer
import okio.source

@ContributesBinding(AppGraph::class, binding<FapRepairApi>())
class FapRepairImpl @Inject constructor(
    private val manifestApi: FapManifestApi,
    private val targetProviderApi: FlipperTargetProviderApi,
    private val fFeatureProvider: FFeatureProvider,
    private val fapDownloadApi: FapDownloadApi,
    private val fapNetworkApi: FapNetworkApi,
    private val queueApi: FapInstallationQueueApi
) : FapRepairApi, LogTagProvider {
    override val TAG = "FapRepairApi"

    override suspend fun check(onProgress: suspend (FapRepairProgress) -> Unit): FapRepairReport {
        onProgress(FapRepairProgress())
        val installed = manifestApi.getManifestFlow()
            .filter { it is FapManifestState.Loaded && !it.inProgress }
            .mapNotNull { (it as? FapManifestState.Loaded)?.items }
            .first()
        val target = targetProviderApi.getFlipperTarget().filterNotNull().first()
            as? FlipperTarget.Received ?: error("Flipper target is not available: can't check apps")
        val storageApi = fFeatureProvider.getSync<FStorageFeatureApi>()
            ?: error("Flipper storage is not available: can't check apps")
        info { "Checking ${installed.size} installed apps" }

        val broken = mutableListOf<FapManifestItem>()
        val uncheckable = mutableListOf<FapManifestItem>()
        for ((index, item) in installed.withIndex()) {
            currentCoroutineContext().ensureActive()
            if (item.externalSource != null) {
                uncheckable += item
                onProgress(FapRepairProgress(index + 1, installed.size, item.fullName, FapRepairStage.CHECKING))
                continue
            }
            onProgress(FapRepairProgress(index, installed.size, item.fullName, FapRepairStage.CHECKING))
            val flipperMd5 = storageApi.md5Api().md5(item.path).getOrNull()
            if (flipperMd5 == null) {
                info { "${item.applicationAlias}: ${item.path} is missing" }
                broken += item
                onProgress(FapRepairProgress(index + 1, installed.size, item.fullName, FapRepairStage.CHECKING))
                continue
            }
            val expectedMd5 = runCatching {
                fapDownloadApi.downloadBundle(target, item.versionUid) { progress ->
                    onProgress(FapRepairProgress(index, installed.size, item.fullName, FapRepairStage.DOWNLOADING, progress))
                }
                    .source().buffer().use { it.readByteString().md5().hex() }
            }.onFailure {
                if (it is CancellationException) throw it
                error(it) { "${item.applicationAlias}: can't get catalog build ${item.versionUid}" }
            }.getOrNull()
            if (expectedMd5 != null && expectedMd5.equals(flipperMd5, ignoreCase = true)) {
                runCatching { fapDownloadApi.discardBundle(target, item.versionUid) }.onFailure {
                    if (it is CancellationException) throw it
                    error(it) { "Could not release checked bundle ${item.versionUid}" }
                }
            }
            when {
                expectedMd5 == null -> uncheckable += item
                !expectedMd5.equals(flipperMd5, ignoreCase = true) -> {
                    info { "${item.applicationAlias}: md5 $flipperMd5 differs from catalog $expectedMd5" }
                    broken += item
                }
            }
            onProgress(FapRepairProgress(index + 1, installed.size, item.fullName, FapRepairStage.CHECKING))
        }
        info { "Check done: ${broken.size} broken, ${uncheckable.size} could not be checked" }
        return FapRepairReport(
            checkedCount = installed.size,
            broken = broken,
            uncheckable = uncheckable
        )
    }

    override suspend fun repair(broken: List<FapManifestItem>, onProgress: suspend (FapRepairProgress) -> Unit): Int {
        val target = targetProviderApi.getFlipperTarget().filterNotNull().first()
        var queued = 0
        for ((index, item) in broken.withIndex()) {
            currentCoroutineContext().ensureActive()
            if (item.externalSource != null) continue
            onProgress(FapRepairProgress(index, broken.size, item.fullName, FapRepairStage.QUEUING))
            val catalogItem = fapNetworkApi.getFapItemById(target, item.uid)
                .onFailure { error(it) { "${item.applicationAlias}: not found in the catalog" } }
                .getOrNull() ?: continue
            queueApi.enqueue(
                FapActionRequest.Update(
                    from = item,
                    toVersion = catalogItem.upToDateVersion,
                    iconUrl = catalogItem.picUrl,
                    applicationName = catalogItem.name
                )
            )
            queued++
            onProgress(FapRepairProgress(index + 1, broken.size, item.fullName, FapRepairStage.QUEUING))
        }
        info { "Queued $queued of ${broken.size} repairs" }
        return queued
    }
}
