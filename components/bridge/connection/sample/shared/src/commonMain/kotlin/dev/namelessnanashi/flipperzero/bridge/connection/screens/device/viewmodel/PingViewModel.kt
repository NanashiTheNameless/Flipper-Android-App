@file:OptIn(ExperimentalStdlibApi::class)

package dev.namelessnanashi.flipperzero.bridge.connection.screens.device.viewmodel

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.getSync
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.wrapToRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.FRpcInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.dataOrNull
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import com.flipperdevices.protobuf.Main
import com.flipperdevices.protobuf.system.PingRequest
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.filterIsInstance
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

@OptIn(ExperimentalCoroutinesApi::class)
class PingViewModel @Inject constructor(
    private val featureProvider: FFeatureProvider,
    orchestrator: FDeviceOrchestrator
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "PingViewModel"

    private val logLines = MutableStateFlow(persistentListOf("Init PingViewModel"))

    init {
        orchestrator.getState().onEach {
            log("Device connect status is: ${it::class.simpleName}")
        }.launchIn(viewModelScope)

        viewModelScope.launch {
            featureProvider.get<FRpcFeatureApi>()
                .flatMapLatest { featureStatusSupport ->
                    log("Receive rpc api status support ${featureStatusSupport::class.simpleName}")
                    if (featureStatusSupport !is FFeatureStatus.Supported) {
                        log("Device api don't support rpc, so skip subscribe on bytes")
                        return@flatMapLatest flowOf()
                    }
                    log("Subscribe to receive bytes flow")
                    featureStatusSupport.featureApi.notificationFlow()
                }.collect {
                    log("Receive bytes: $it ")
                }
        }
        viewModelScope.launch {
            val alreadyPrintedKeys = mutableSetOf<String>()
            featureProvider.get<FRpcInfoFeatureApi>()
                .filterIsInstance<FFeatureStatus.Supported<FRpcInfoFeatureApi>>()
                .flatMapLatest {
                    it.featureApi.getRpcInformationFlow()
                }.onEach { rpcInfo ->
                    val data = rpcInfo.dataOrNull()
                    if (data == null) {
                        log("RpcInfo feature is $rpcInfo")
                    } else {
                        for ((key, value) in data.allFields.entries) {
                            if (alreadyPrintedKeys.contains(key).not()) {
                                alreadyPrintedKeys.add(key)
                                log("$key: $value")
                            }
                        }
                    }
                    if (rpcInfo is FlipperInformationStatus.Ready) {
                        log("Request complete")
                    }
                }.launchIn(viewModelScope)
        }
    }

    fun getLogLinesState() = logLines.asStateFlow()

    fun sendPing() = viewModelScope.launch {
        log("Request send ping")
        val requestApi = featureProvider.getSync<FRpcFeatureApi>()
        info { "Receive requestApi: $requestApi" }
        if (requestApi == null) {
            error { "Failed receive request api" }
            log("Failed receive request api")
        } else {
            requestApi.requestWithoutAnswer(
                Main(
                    system_ping_request = PingRequest()
                ).wrapToRequest()
            )
            info { "Send ping request successful" }
            log("Send ping request successful")
        }
    }

    fun invalidateRpcInfo() = viewModelScope.launch {
        val rpcInfoApi = featureProvider.get<FRpcInfoFeatureApi>()
            .filterIsInstance<FFeatureStatus.Supported<FRpcInfoFeatureApi>>()
            .first().featureApi
        rpcInfoApi.invalidate(viewModelScope, force = true)
    }

    private fun log(text: String) {
        logLines.update { it.add(text) }
    }
}
