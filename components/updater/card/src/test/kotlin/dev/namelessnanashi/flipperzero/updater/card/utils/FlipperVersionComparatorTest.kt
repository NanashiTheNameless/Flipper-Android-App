package dev.namelessnanashi.flipperzero.updater.card.utils

import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import org.junit.Assert
import org.junit.Test

class FlipperVersionComparatorTest {
    private fun release(version: String) = FirmwareVersion(
        channel = FirmwareChannel.RELEASE,
        version = version
    )

    @Test
    fun `newer momentum release is greater`() {
        Assert.assertEquals(true, release("mntm-013").isGreaterThan(release("mntm-012")))
    }

    @Test
    fun `same momentum release is not greater`() {
        Assert.assertEquals(false, release("mntm-012").isGreaterThan(release("mntm-012")))
    }

    @Test
    fun `older momentum release is not greater`() {
        Assert.assertEquals(false, release("mntm-011").isGreaterThan(release("mntm-012")))
    }

    @Test
    fun `different channels are not comparable`() {
        val dev = FirmwareVersion(channel = FirmwareChannel.DEV, version = "d3f89dfe")
        Assert.assertNull(release("mntm-012").isGreaterThan(dev))
    }
}
