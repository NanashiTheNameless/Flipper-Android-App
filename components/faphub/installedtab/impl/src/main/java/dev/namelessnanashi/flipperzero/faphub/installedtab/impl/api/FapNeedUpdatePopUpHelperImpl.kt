package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.ktx.jre.launchWithLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapNeedUpdatePopUpHelper
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapInstalledInternalState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.FapInstalledInternalLoadingState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.InstalledFapsFromNetworkProducer
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FapNeedUpdatePopUpHelper>())
class FapNeedUpdatePopUpHelperImpl @Inject constructor(
    globalScopeProvider: Provider<CoroutineScope>,
    inAppNotificationStorageProvider: Provider<InAppNotificationStorage>,
    installedFapsNetworkProducerProvider: Provider<InstalledFapsFromNetworkProducer>
) : FapNeedUpdatePopUpHelper, LogTagProvider {
    override val TAG = "FapNeedUpdatePopUpHelper"

    private val globalScope by globalScopeProvider
    private val inAppNotificationStorage by inAppNotificationStorageProvider
    private val installedFapsNetworkProducer by installedFapsNetworkProducerProvider

    private val mutex = Mutex()
    private var notificationJob: Job? = null

    override fun notifyIfUpdateAvailable() = launchWithLock(mutex, globalScope, "notify") {
        val oldJob = notificationJob
        notificationJob = globalScope.launch {
            oldJob?.cancelAndJoin()
            installedFapsNetworkProducer.refresh(force = false)
            val loadedFaps = installedFapsNetworkProducer.getLoadedFapsFlow()
                .filterIsInstance<FapInstalledInternalLoadingState.Loaded>()
                .first()
            info { "Loaded faps $loadedFaps" }
            val readyToUpdateCount = loadedFaps
                .faps
                .count { (_, state) -> state is FapInstalledInternalState.ReadyToUpdate }
            info { "Ready to update count $loadedFaps" }
            if (readyToUpdateCount > 0) {
                inAppNotificationStorage.addNotification(InAppNotification.ReadyToUpdateFaps)
            }
        }
    }
}
