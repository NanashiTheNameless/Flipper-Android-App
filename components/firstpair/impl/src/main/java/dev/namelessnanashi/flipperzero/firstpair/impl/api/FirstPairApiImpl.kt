package dev.namelessnanashi.flipperzero.firstpair.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.firstpair.api.FirstPairApi
import dev.namelessnanashi.flipperzero.firstpair.impl.storage.FirstPairStorage
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@ContributesBinding(AppGraph::class)
class FirstPairApiImpl @Inject constructor(
    private val firstPairStorage: FirstPairStorage
) : FirstPairApi {
    override fun shouldWeOpenPairScreen(): Boolean {
        return firstPairStorage.isTosPassed().not() || firstPairStorage.isDeviceSelected().not()
    }
}
