package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.FRpcInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.fullinforpc.DeprecatedFlipperFullInfoRpcApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.fullinforpc.NewFlipperFullInfoRpcApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex

class FRpcInfoFeatureApiImpl @AssistedInject constructor(
    @Assisted private val rpcFeatureApi: FRpcFeatureApi,
    @Assisted private val getInfoFeatureApi: FGetInfoFeatureApi?,
) : FRpcInfoFeatureApi, LogTagProvider {
    override val TAG = "FlipperRpcInformationApi"

    private val mutex = Mutex()
    private var alreadyRequested = false
    private var job: Job? = null
    private val rpcInformationFlow =
        MutableStateFlow<FlipperInformationStatus<FlipperRpcInformation>>(
            FlipperInformationStatus.NotStarted()
        )

    override fun getRpcInformationFlow() = rpcInformationFlow.asStateFlow()

    override suspend fun invalidate(
        scope: CoroutineScope,
        force: Boolean
    ) = withLock(mutex, "invalidate") {
        // A caller's scope can cancel an unfinished load. Allow the next caller
        // to retry instead of leaving the cached state permanently InProgress.
        if (force.not() && alreadyRequested &&
            (job?.isActive == true || rpcInformationFlow.value is FlipperInformationStatus.Ready)
        ) {
            return@withLock
        }
        alreadyRequested = true

        job?.cancelAndJoin()
        job = scope.launch {
            invalidateInternal()
        }
    }

    override suspend fun reset() = withLock(mutex, "reset") {
        alreadyRequested = false
        job?.cancelAndJoin()
        rpcInformationFlow.emit(FlipperInformationStatus.NotStarted())
    }

    private suspend fun invalidateInternal() {
        rpcInformationFlow.emit(FlipperInformationStatus.InProgress(FlipperRpcInformation()))
        val flipperFullInfoRpcApi = if (
            getInfoFeatureApi != null
        ) {
            NewFlipperFullInfoRpcApi(getInfoFeatureApi)
        } else {
            DeprecatedFlipperFullInfoRpcApi()
        }

        flipperFullInfoRpcApi
            .getRawDataFlow(rpcFeatureApi)
            .collect { rpcInformation ->
                rpcInformationFlow.update {
                    if (it is FlipperInformationStatus.InProgress) {
                        FlipperInformationStatus.InProgress(rpcInformation)
                    } else {
                        it
                    }
                }
            }

        rpcInformationFlow.update {
            if (it is FlipperInformationStatus.InProgress) {
                FlipperInformationStatus.Ready(it.data)
            } else {
                it
            }
        }
    }

    @AssistedFactory
    fun interface InternalFactory {
        operator fun invoke(
            rpcFeatureApi: FRpcFeatureApi,
            getInfoFeatureApi: FGetInfoFeatureApi?,
        ): FRpcInfoFeatureApiImpl
    }
}
