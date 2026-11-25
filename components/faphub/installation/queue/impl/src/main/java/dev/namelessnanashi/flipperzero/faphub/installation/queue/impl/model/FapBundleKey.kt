package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.model

import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget

internal fun FapActionRequest.bundleKey(): String? {
    val version = when (this) {
        is FapActionRequest.Install -> toVersion
        is FapActionRequest.Update -> toVersion
        else -> return null
    }
    val target = version.target as? FlipperTarget.Received ?: return null
    return "${version.id}_${target.target}_${target.sdk}"
}
