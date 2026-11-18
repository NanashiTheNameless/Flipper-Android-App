package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.utils

import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker

internal fun detailedProgressWrapperTrackerStub(): DetailedProgressWrapperTracker {
    return DetailedProgressWrapperTracker(min = 0f, max = 1.0f, progressListener = { _, _ -> })
}
