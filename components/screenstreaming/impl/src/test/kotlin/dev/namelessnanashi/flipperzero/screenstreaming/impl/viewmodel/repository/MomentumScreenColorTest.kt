package dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.repository

import org.junit.Assert
import org.junit.Test

class MomentumScreenColorTest {
    @Test
    fun `default mode uses the app colour`() {
        Assert.assertNull(MomentumScreenColor.resolve(packed = 0, rainbowHueDegrees = { 0f }))
    }

    @Test
    fun `custom mode unpacks red green blue`() {
        // mode 1, r = 0x12, g = 0x34, b = 0x56
        val packed = 1 or (0x12 shl 8) or (0x34 shl 16) or (0x56 shl 24)

        val color = MomentumScreenColor.resolve(packed, rainbowHueDegrees = { 0f })

        Assert.assertEquals(0xFF123456.toInt(), color)
    }

    @Test
    fun `custom mode handles a set top bit`() {
        val packed = 1 or (0x00 shl 8) or (0x80 shl 16) or (0xFF shl 24)

        Assert.assertEquals(0xFF0080FF.toInt(), MomentumScreenColor.resolve(packed, rainbowHueDegrees = { 0f }))
    }
}
