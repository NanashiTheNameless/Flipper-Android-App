package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model

import dev.namelessnanashi.flipperzero.core.ktx.jre.TimeHelper
import com.flipperdevices.protobuf.Main

data class FlipperRequest(
    val data: Main,
    val createTimestampNanos: Long = TimeHelper.getNanoTime(),
    val priority: FlipperRequestPriority = FlipperRequestPriority.DEFAULT,
    val onSendCallback: (suspend () -> Unit)? = null
)

fun Main.wrapToRequest(
    priority: FlipperRequestPriority = FlipperRequestPriority.DEFAULT
) = FlipperRequest(data = this, priority = priority)
