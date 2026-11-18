package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.impl

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class ForeignClientDetectorTest {
    @Test
    fun `warns after threshold unknown answers within the window`() {
        val detector = ForeignClientDetector(threshold = 3, windowMs = 1000, cooldownMs = 10_000)

        assertFalse(detector.onUnknownAnswer(nowMs = 0))
        assertFalse(detector.onUnknownAnswer(nowMs = 100))
        assertTrue(detector.onUnknownAnswer(nowMs = 200))
    }

    @Test
    fun `old unknown answers fall out of the window`() {
        val detector = ForeignClientDetector(threshold = 3, windowMs = 1000, cooldownMs = 10_000)

        detector.onUnknownAnswer(nowMs = 0)
        detector.onUnknownAnswer(nowMs = 100)
        assertFalse(detector.onUnknownAnswer(nowMs = 2000))
    }

    @Test
    fun `warns at most once per cooldown`() {
        val detector = ForeignClientDetector(threshold = 1, windowMs = 1000, cooldownMs = 10_000)

        assertTrue(detector.onUnknownAnswer(nowMs = 0))
        assertFalse(detector.onUnknownAnswer(nowMs = 5000))
        assertTrue(detector.onUnknownAnswer(nowMs = 11_000))
    }
}
