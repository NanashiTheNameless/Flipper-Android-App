package dev.namelessnanashi.flipperzero.inappnotification.impl.storage

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationListener
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.SupervisorJob
import java.util.Stack
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.SingleIn
import kotlin.time.DurationUnit
import kotlin.time.toDuration
import dev.zacsweers.metro.binding

private val TIMER_DELAY = 1.toDuration(DurationUnit.SECONDS)

@SingleIn(AppGraph::class)
@ContributesBinding(AppGraph::class, binding<InAppNotificationStorage>())
class InAppNotificationStorageImpl @Inject constructor() :
    InAppNotificationStorage,
    LogTagProvider {
    override val TAG = "InAppNotificationStorage"
    private val coroutineScope = CoroutineScope(SupervisorJob())
    private val timerTask = TimerTask(
        delayDuration = TIMER_DELAY,
        coroutineScope = coroutineScope,
        block = { invalidate() }
    )

    private val pendingNotification = Stack<InAppNotification>()
    private var listener: InAppNotificationListener? = null
    private var nextNotificationTime = 0L

    override fun subscribe(listener: InAppNotificationListener) {
        info { "#subscribe. Current listener: ${this.listener}, updated: $listener" }
        if (this.listener != null) {
            unsubscribe()
        }
        this.listener = listener
        timerTask.start()
        invalidate()
    }

    override fun unsubscribe() {
        info { "#unsubscribe. Current listener: ${this.listener}" }
        this.listener = null
        timerTask.shutdown()
    }

    @Synchronized
    override fun addNotification(notification: InAppNotification) {
        pendingNotification.push(notification)
        invalidate()
    }

    @Synchronized
    private fun invalidate() {
        val notificationListener = listener ?: return

        val currentTime = System.currentTimeMillis()
        if (currentTime < nextNotificationTime) {
            return
        }

        if (pendingNotification.empty()) {
            return
        }

        val notificationToShown = pendingNotification.pop()
        runBlockingWithLog {
            notificationListener.onNewNotification(notificationToShown)
        }
        nextNotificationTime = System.currentTimeMillis() + notificationToShown.durationMs
    }
}
