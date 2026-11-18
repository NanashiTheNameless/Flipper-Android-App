package dev.namelessnanashi.flipperzero.app.background

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.content.ContextCompat
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
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.transformLatest
import kotlinx.coroutines.withContext

// A dropped link reconnects on its own; keep the process alive across that gap
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
    enum class Phase { CONNECTING, CONNECTED, SYNCING }
}

/**
 * Fork: upstream removed its Android service with the old bridge, so nothing kept the process
 * alive once the app left the screen and Android froze or killed it mid-sync or mid-transfer.
 * This keeps [FlipperConnectionService] (a `connectedDevice` foreground service) running while
 * a Flipper is connected, connecting or syncing, and for a short grace period after a drop.
 */
@SingleIn(AppGraph::class)
class ConnectionKeepAlive @Inject constructor(
    private val context: Context,
    private val orchestrator: FDeviceOrchestrator,
    private val synchronizationApi: SynchronizationApi,
    private val permissionRequestHandler: PermissionRequestHandler
) : LogTagProvider {
    override val TAG = "ConnectionKeepAlive"

    private val scope = CoroutineScope(SupervisorJob() + FlipperDispatchers.workStealingDispatcher)
    private val status = MutableStateFlow<KeepAliveStatus?>(null)
    private var notificationPermissionAsked = false

    fun getStatus(): StateFlow<KeepAliveStatus?> = status.asStateFlow()

    @OptIn(ExperimentalCoroutinesApi::class)
    fun start() {
        combine(
            orchestrator.getState(),
            synchronizationApi.getSynchronizationState(),
            ::toStatus
        ).transformLatest { newStatus ->
            if (newStatus == null) {
                delay(DISCONNECTED_GRACE_MS)
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
        sync: SynchronizationState
    ): KeepAliveStatus? {
        val device = when (connection) {
            is FDeviceConnectStatus.Connected -> connection.device
            is FDeviceConnectStatus.Connecting -> connection.device
            is FDeviceConnectStatus.Disconnecting -> connection.device
            is FDeviceConnectStatus.Disconnected -> connection.device
        }
        val name = device?.humanReadableName ?: "Flipper"
        if (sync is SynchronizationState.InProgress) {
            return KeepAliveStatus(name, KeepAliveStatus.Phase.SYNCING, sync.progress.coerceIn(0f, 1f))
        }
        return when (connection) {
            is FDeviceConnectStatus.Connected -> KeepAliveStatus(name, KeepAliveStatus.Phase.CONNECTED)
            is FDeviceConnectStatus.Connecting,
            is FDeviceConnectStatus.Disconnecting -> KeepAliveStatus(name, KeepAliveStatus.Phase.CONNECTING)
            is FDeviceConnectStatus.Disconnected -> null
        }
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
