package dev.namelessnanashi.flipperzero.faphub.source.impl

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Intent
import android.os.IBinder
import android.os.PowerManager
import androidx.core.app.NotificationCompat
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.ComponentHolder
import dev.zacsweers.metro.ContributesTo
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.launch

@ContributesTo(AppGraph::class)
interface SourceComponent {
    val sourceJobs: () -> SourceJobs
    val sourceBackup: () -> SourceBackup
}

class SourceBuildService : Service() {
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private val jobs by lazy { ComponentHolder.component<SourceComponent>().sourceJobs() }
    private var wakeLock: PowerManager.WakeLock? = null

    override fun onCreate() {
        super.onCreate()
        wakeLock = getSystemService(PowerManager::class.java).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "FlipperNanashi:AppSources").apply {
            acquire(50 * 60_000L)
        }
        val manager = getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(NotificationChannel("app_sources", getString(R.string.source_notification_channel), NotificationManager.IMPORTANCE_LOW))
        val open = PendingIntent.getActivity(this, 0, Intent(this, SourceActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
        val cancel = PendingIntent.getService(this, 1, Intent(this, SourceBuildService::class.java).setAction("cancel"), PendingIntent.FLAG_IMMUTABLE)
        startForeground(0x4642, NotificationCompat.Builder(this, "app_sources")
            .setSmallIcon(android.R.drawable.stat_sys_download)
            .setContentTitle(getString(R.string.source_notification_title))
            .setContentText(getString(R.string.source_notification_text))
            .setContentIntent(open).setOngoing(true)
            .addAction(0, getString(R.string.source_cancel), cancel).build())
        scope.launch {
            jobs.active.collect { if (it == 0) stopSelf() }
        }
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        if (intent?.action == "cancel") jobs.cancelAll()
        return START_NOT_STICKY
    }

    override fun onBind(intent: Intent?): IBinder? = null

    override fun onDestroy() {
        wakeLock?.takeIf { it.isHeld }?.release()
        scope.cancel()
        super.onDestroy()
    }

    override fun onTimeout(startId: Int, fgsType: Int) {
        jobs.cancelAll()
        stopSelf()
    }
}
