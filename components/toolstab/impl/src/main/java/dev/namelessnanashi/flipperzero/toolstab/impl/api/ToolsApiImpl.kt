package dev.namelessnanashi.flipperzero.toolstab.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.nfc.mfkey32.api.MfKey32Api
import dev.namelessnanashi.flipperzero.toolstab.api.ToolsApi
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.flow.Flow
import dev.zacsweers.metro.Inject

@ContributesBinding(AppGraph::class)
class ToolsApiImpl @Inject constructor(
    private val mfKey32Api: MfKey32Api
) : ToolsApi {
    override fun hasNotification(scope: CoroutineScope): Flow<Boolean> {
        return mfKey32Api.hasNotification()
    }
}
