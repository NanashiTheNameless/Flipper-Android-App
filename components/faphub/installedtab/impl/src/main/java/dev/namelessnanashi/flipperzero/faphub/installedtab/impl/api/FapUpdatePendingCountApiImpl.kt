package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapUpdatePendingCountApi
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.model.FapInstalledInternalState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.FapInstalledInternalLoadingState
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel.InstalledFapsFromNetworkProducer
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FapUpdatePendingCountApi>())
class FapUpdatePendingCountApiImpl @Inject constructor(
    fapsStateProducerProvider: Provider<InstalledFapsFromNetworkProducer>
) : FapUpdatePendingCountApi {
    private val fapsStateProducer by fapsStateProducerProvider

    override fun getUpdatePendingCount(): Flow<Int> {
        fapsStateProducer.refresh(force = false)
        return fapsStateProducer.getLoadedFapsFlow().map { loadingState ->
            when (loadingState) {
                is FapInstalledInternalLoadingState.Error -> 0

                is FapInstalledInternalLoadingState.Loaded -> loadingState.faps.count { (_, state) ->
                    state is FapInstalledInternalState.ReadyToUpdate
                }
            }
        }
    }
}
