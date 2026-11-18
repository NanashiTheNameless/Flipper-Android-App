package dev.namelessnanashi.flipperzero.buildlogic

import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * Seconds since 2026-01-01T00:00:00Z, so every build installs over the previous one.
 * Fits in a signed Int until 2094-01-19. A ValueSource is read at execution time,
 * so a reused configuration cache does not freeze the value.
 */
abstract class BuildVersionCode : ValueSource<Int, ValueSourceParameters.None> {
    override fun obtain(): Int {
        val seconds = System.currentTimeMillis() / MILLIS_IN_SECOND - EPOCH_2026_SECONDS
        return seconds.coerceIn(1L, Int.MAX_VALUE.toLong()).toInt()
    }

    private companion object {
        const val EPOCH_2026_SECONDS = 1_767_225_600L
        const val MILLIS_IN_SECOND = 1000L
    }
}
