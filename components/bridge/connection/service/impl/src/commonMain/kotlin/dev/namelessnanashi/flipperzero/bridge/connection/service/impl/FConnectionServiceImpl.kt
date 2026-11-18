package dev.namelessnanashi.flipperzero.bridge.connection.service.impl

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.DisconnectStatus
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.connection.service.api.FConnectionService
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.warn
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import dev.zacsweers.metro.binding

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<FConnectionService>())
class FConnectionServiceImpl @Inject constructor(
    private val orchestrator: FDeviceOrchestrator,
    private val fDevicePersistedStorage: FDevicePersistedStorage
) : FConnectionService, LogTagProvider {
    override val TAG: String = "FConnectionService"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val mutex = Mutex()
    private val isForceDisconnected = MutableStateFlow(false)

    private fun getBrokenConnectionReconnectJob(scope: CoroutineScope): Job {
        return orchestrator.getState()
            .onEach { status ->
                if (status !is FDeviceConnectStatus.Disconnected) return@onEach
                when (status.reason) {
                    DisconnectStatus.NOT_INITIALIZED -> return@onEach
                    DisconnectStatus.REPORTED_BY_TRANSPORT -> Unit
                    DisconnectStatus.ERROR_UNKNOWN -> Unit
                }
                if (isForceDisconnected.first()) return@onEach
                val currentDevice = status.device ?: return@onEach
                orchestrator.disconnectCurrent()
                orchestrator.connect(currentDevice)
            }.launchIn(scope)
    }

    private fun getConnectionJob(scope: CoroutineScope): Job {
        return combine(
            flow = fDevicePersistedStorage.getCurrentDevice(),
            flow2 = isForceDisconnected,
            transform = { currentDevice, isForceDisconnected ->
                when {
                    isForceDisconnected -> orchestrator.disconnectCurrent()
                    currentDevice == null -> orchestrator.disconnectCurrent()

                    else -> orchestrator.connect(currentDevice)
                }
            }
        ).launchIn(scope)
    }

    override fun onApplicationInit() {
        scope.launch {
            if (mutex.isLocked) {
                warn { "#onApplicationInit tried to init connection service again" }
                return@launch
            }
            mutex.withLock {
                val brokenConnectionReconnectJob = getBrokenConnectionReconnectJob(this)
                val connectionJob = getConnectionJob(this)
                connectionJob.join()
                brokenConnectionReconnectJob.join()
            }
        }
    }

    override fun forceReconnect() {
        scope.launch { isForceDisconnected.emit(false) }
    }

    override fun disconnect(force: Boolean) {
        scope.launch {
            isForceDisconnected.emit(force)
        }
    }

    override fun forgetCurrentDevice() {
        scope.launch {
            fDevicePersistedStorage.getCurrentDevice()
                .first()
                ?.let { currentDevice ->
                    fDevicePersistedStorage.removeDevice(currentDevice.uniqueId)
                }
        }
    }

    override fun connectIfNotForceDisconnect() {
        scope.launch {
            if (isForceDisconnected.first()) return@launch
            val currentDevice = fDevicePersistedStorage.getCurrentDevice()
                .first()
                ?: return@launch
            orchestrator.connect(currentDevice)
        }
    }
}
