package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import org.junit.Assert.assertEquals
import org.junit.Test

class KeysArchiveNameTest {
    @Test
    fun `uses the flipper name`() {
        assertEquals("Flipper-Nanashi-Keys.zip", keysArchiveName("Nanashi"))
    }

    @Test
    fun `drops a Flipper prefix from the reported name`() {
        assertEquals("Flipper-Nanashi-Keys.zip", keysArchiveName("Flipper Nanashi"))
    }

    @Test
    fun `replaces characters that are unsafe in a file name`() {
        assertEquals("Flipper-My_Zero-Keys.zip", keysArchiveName("My Zero/"))
    }

    @Test
    fun `falls back without a name`() {
        assertEquals("Flipper-Keys.zip", keysArchiveName(null))
        assertEquals("Flipper-Keys.zip", keysArchiveName("  "))
        assertEquals("Flipper-Keys.zip", keysArchiveName("///"))
    }
}
