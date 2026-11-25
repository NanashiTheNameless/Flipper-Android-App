package dev.namelessnanashi.flipperzero.app.background

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.seriallagsdetector.api.FLagsDetectorFeature
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.permission.api.PermissionRequestHandler
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.FapInstallationQueueApi
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapQueueState
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

// Work in flight comes in bursts; don't stop the service in the gaps between requests
private const val IDLE_GRACE_MS = 5_000L

// A link that drops mid-work reconnects on its own; keep the process alive across that gap
private const val DISCONNECTED_GRACE_MS = 15_000L

/**
 * What the background notification shows; null when nothing needs the app to stay alive.
 */
data class KeepAliveStatus(
    val deviceName: String,
    val phase: Phase,
    // 0..1 while syncing
    val progress: Float? = null
) {
    enum class Phase { RECONNECTING, SYNCING, INSTALLING, TRANSFERRING }
}

/**
 * Fork: upstream removed its Android service with the old bridge, so nothing kept the process
 * alive once the app left the screen and Android froze or killed it mid-sync or mid-transfer.
 *
 * This keeps [FlipperConnectionService] (a `connectedDevice` foreground service) running only
 * while work is in flight: a sync, FapHub installs/updates, or any request still waiting for
 * the Flipper's answer (file transfers, firmware upload). An idle connection does not keep the
 * app running in the background; the service stops a few seconds after the work ends.
 */
@SingleIn(AppGraph::class)
class ConnectionKeepAlive @Inject constructor(
    private val context: Context,
    private val orchestrator: FDeviceOrchestrator,
    private val synchronizationApi: SynchronizationApi,
    private val featureProvider: FFeatureProvider,
    private val fapQueueApi: FapInstallationQueueApi,
    private val permissionRequestHandler: PermissionRequestHandler
) : LogTagProvider {
    override val TAG = "ConnectionKeepAlive"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val status = MutableStateFlow<KeepAliveStatus?>(null)
    private var notificationPermissionAsked = false

    fun getStatus(): StateFlow<KeepAliveStatus?> = status.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        val hasPendingRequests = featureProvider.get<FLagsDetectorFeature>()
            .flatMapLatest { featureStatus ->
                (featureStatus as? FFeatureStatus.Supported<FLagsDetectorFeature>)
                    ?.featureApi
                    ?.hasPendingRequests()
                    ?: flowOf(false)
            }.distinctUntilChanged()
        val hasQueuedApps = fapQueueApi.getAllTasks()
            .map { tasks -> tasks.any { it is FapQueueState.Pending || it is FapQueueState.InProgress } }
            .distinctUntilChanged()

        combine(
            orchestrator.getState(),
            synchronizationApi.getSynchronizationState(),
            hasQueuedApps,
            hasPendingRequests,
            ::toStatus
        ).transformLatest { (newStatus, connection) ->
            if (newStatus == null) {
                delay(
                    if (connection is FDeviceConnectStatus.Disconnected) {
                        DISCONNECTED_GRACE_MS
                    } else {
                        IDLE_GRACE_MS
                    }
                )
            }
            emit(newStatus)
        }.distinctUntilChanged()
            .onEach { newStatus ->
                status.value = newStatus
                if (newStatus != null) {
                    startService()
                }
            }
            .launchIn(scope)
    }

    private fun toStatus(
        connection: FDeviceConnectStatus,
        sync: SynchronizationState,
        hasQueuedApps: Boolean,
        hasPendingRequests: Boolean
    ): Pair<KeepAliveStatus?, FDeviceConnectStatus> {
        val device = when (connection) {
            is FDeviceConnectStatus.Connected -> connection.device
            is FDeviceConnectStatus.Connecting -> connection.device
            is FDeviceConnectStatus.Disconnecting -> connection.device
            is FDeviceConnectStatus.Disconnected -> connection.device
        }
        val name = device?.humanReadableName ?: "Flipper"
        val isSyncing = sync is SynchronizationState.InProgress
        val isBusy = isSyncing || hasQueuedApps || hasPendingRequests
        val newStatus = when {
            !isBusy || connection is FDeviceConnectStatus.Disconnected -> null
            connection !is FDeviceConnectStatus.Connected ->
                KeepAliveStatus(name, KeepAliveStatus.Phase.RECONNECTING)
            sync is SynchronizationState.InProgress -> KeepAliveStatus(
                name,
                KeepAliveStatus.Phase.SYNCING,
                sync.progress.coerceIn(0f, 1f)
            )
            hasQueuedApps -> KeepAliveStatus(name, KeepAliveStatus.Phase.INSTALLING)
            else -> KeepAliveStatus(name, KeepAliveStatus.Phase.TRANSFERRING)
        }
        return newStatus to connection
    }

    private suspend fun startService() {
        if (FlipperConnectionService.isRunning) return
        info { "Start the connection foreground service" }
        try {
            ContextCompat.startForegroundService(
                context,
                Intent(context, FlipperConnectionService::class.java)
            )
        } catch (e: IllegalStateException) {
            // Android 12+ refuses to start a foreground service while the app is in the
            // background (for example when the widget woke the process). The next connect
            // with the app open starts it.
            error(e) { "Could not start the connection foreground service" }
        }
        askNotificationPermission()
    }

    /**
     * Android 13+ hides the service's notification without this. Asked once per launch; the
     * service runs either way.
     */
    private suspend fun askNotificationPermission() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU || notificationPermissionAsked) return
        val permission = Manifest.permission.POST_NOTIFICATIONS
        if (ContextCompat.checkSelfPermission(context, permission) == PackageManager.PERMISSION_GRANTED) return
        notificationPermissionAsked = true
        withContext(Dispatchers.Main) {
            permissionRequestHandler.requestPermission(permission) { _, granted ->
                info { "Notification permission granted: $granted" }
            }
        }
    }
}
