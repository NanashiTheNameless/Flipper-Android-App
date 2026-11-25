package dev.namelessnanashi.flipperzero.core.preference.internal

import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import kotlinx.coroutines.test.runTest
import okio.ByteString.Companion.decodeHex
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class RemovePolicyAcceptanceMigrationTest {
    @Test
    fun `removes old acceptance and retains setup and unrelated unknown fields`() = runTest {
        // Retired bool (tag 1), completed setup (tag 2), unknown future bool (tag 10).
        val old = PairSettings.ADAPTER.decode("080110015001".decodeHex())

        assertTrue(RemovePolicyAcceptanceMigration.shouldMigrate(old))
        val migrated = RemovePolicyAcceptanceMigration.migrate(old)

        assertTrue(migrated.pair_device_pass)
        assertEquals("10015001".decodeHex(), migrated.encodeByteString())
        assertFalse(RemovePolicyAcceptanceMigration.shouldMigrate(migrated))
    }
}
