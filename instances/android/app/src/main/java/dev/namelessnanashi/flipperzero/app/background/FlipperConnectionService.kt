package dev.namelessnanashi.flipperzero.app.background

import android.app.Notification
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.Build
import android.os.IBinder
import androidx.core.app.NotificationChannelCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.app.ServiceCompat
import dev.namelessnanashi.flipperzero.app.R
import dev.namelessnanashi.flipperzero.app.di.MainComponent
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach

private const val CHANNEL_ID = "flipper_connection"
private const val NOTIFICATION_ID = 0x464C // "FL"
private const val ACTION_DISCONNECT = "dev.namelessnanashi.flipperzero.action.DISCONNECT"
private const val PERCENT = 100

/**
 * Foreground service (type `connectedDevice`) that keeps the app process alive while a Flipper
 * is connected or syncing, so leaving the app no longer freezes or kills the connection. It
 * follows [ConnectionKeepAlive.getStatus] and stops itself when that becomes null.
 */
class FlipperConnectionService : Service(), LogTagProvider {
    override val TAG = "FlipperConnectionService"

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val component by lazy { ComponentHolder.component<MainComponent>() }
    private var isForeground = false

    override fun onCreate() {
        super.onCreate()
        isRunning = true
        NotificationManagerCompat.from(this).createNotificationChannel(
            NotificationChannelCompat.Builder(CHANNEL_ID, NotificationManagerCompat.IMPORTANCE_LOW)
                .setName(getString(R.string.connection_notification_channel))
                .setShowBadge(false)
                .build()
        )
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == ACTION_DISCONNECT) {
            info { "Disconnect from the notification" }
            component.fConnectionService.invoke().disconnect(force = true)
        }
        val keepAlive = component.connectionKeepAlive.invoke()
        // startForegroundService() requires startForeground() soon after, even when stopping
        if (!enterForeground(keepAlive.getStatus().value)) {
            return START_NOT_STICKY
        }
        if (startId == 1) {
            keepAlive.getStatus().onEach { status ->
                if (status == null) {
                    info { "Nothing keeps the app alive any more, stop" }
                    ServiceCompat.stopForeground(this, ServiceCompat.STOP_FOREGROUND_REMOVE)
                    stopSelf()
                } else {
                    NotificationManagerCompat.from(this).runCatching {
                        notify(NOTIFICATION_ID, buildNotification(status))
                    }
                }
            }.launchIn(scope)
        }
        return START_NOT_STICKY
    }

    private fun enterForeground(status: KeepAliveStatus?): Boolean {
        if (isForeground) return true
        return try {
            ServiceCompat.startForeground(
                this,
                NOTIFICATION_ID,
                buildNotification(status),
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    ServiceInfo.FOREGROUND_SERVICE_TYPE_CONNECTED_DEVICE
                } else {
                    0
                }
            )
            isForeground = true
            true
        } catch (e: RuntimeException) {
            // Missing prerequisite permission or a background start: run without the service
            error(e) { "Could not enter the foreground" }
            stopSelf()
            false
        }
    }

    private fun buildNotification(status: KeepAliveStatus?): Notification {
        val name = status?.deviceName ?: getString(R.string.connection_notification_flipper)
        val title = when (status?.phase) {
            KeepAliveStatus.Phase.SYNCING -> getString(R.string.connection_notification_syncing, name)
            KeepAliveStatus.Phase.CONNECTED -> getString(R.string.connection_notification_connected, name)
            KeepAliveStatus.Phase.CONNECTING, null ->
                getString(R.string.connection_notification_connecting, name)
        }
        val openApp = packageManager.getLaunchIntentForPackage(packageName)?.let {
            PendingIntent.getActivity(this, 0, it, PendingIntent.FLAG_IMMUTABLE)
        }
        val disconnect = PendingIntent.getService(
            this,
            0,
            Intent(this, FlipperConnectionService::class.java).setAction(ACTION_DISCONNECT),
            PendingIntent.FLAG_IMMUTABLE
        )
        return NotificationCompat.Builder(this, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification_flipper)
            .setContentTitle(title)
            .setContentIntent(openApp)
            .setOngoing(true)
            .setOnlyAlertOnce(true)
            .setSilent(true)
            .setCategory(NotificationCompat.CATEGORY_SERVICE)
            .setForegroundServiceBehavior(NotificationCompat.FOREGROUND_SERVICE_IMMEDIATE)
            .addAction(0, getString(R.string.connection_notification_disconnect), disconnect)
            .apply {
                val progress = status?.progress
                if (status?.phase == KeepAliveStatus.Phase.SYNCING && progress != null) {
                    setProgress(PERCENT, (progress * PERCENT).toInt(), false)
                    setContentText("${(progress * PERCENT).toInt()}%")
                }
            }
            .build()
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        isRunning = false
        scope.cancel()
        super.onDestroy()
    }

    companion object {
        @Volatile
        var isRunning = false
            private set
    }
}
