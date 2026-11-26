package dev.namelessnanashi.flipperzero.bridge.dao.impl.api

import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.AppDatabase
import dev.namelessnanashi.flipperzero.bridge.dao.impl.api.key.SimpleKeyApiImpl
import dev.namelessnanashi.flipperzero.bridge.dao.impl.converters.DatabaseKeyContentConverter
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.DatabaseKeyContent
import io.mockk.every
import io.mockk.mockk
import kotlinx.collections.immutable.persistentListOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test
import org.junit.Rule
import org.junit.rules.TemporaryFolder
import java.io.File
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class KeyRestoreTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun database(): AppDatabase {
        val converter = mockk<DatabaseKeyContentConverter> {
            every { keyContentToPath(any()) } answers {
                firstArg<DatabaseKeyContent?>()?.let { content ->
                    temporary.newFile().apply { content.flipperContent.openStream().use { writeBytes(it.readBytes()) } }.absolutePath
                }
            }
            every { pathToKeyContent(any()) } answers {
                firstArg<String?>()?.let { DatabaseKeyContent(FlipperKeyContent.InternalFile(it)) }
            }
        }
        return Room.inMemoryDatabaseBuilder(ApplicationProvider.getApplicationContext(), AppDatabase::class.java)
            .addTypeConverter(converter).build()
    }

    @Test
    fun `restoring matching paths updates contents and notes while preserving identities`() = runTest {
        val database = database()
        try {
            val keys = SimpleKeyApiImpl({ database.keyDao() }, { database.additionalFileDao() }, { database })
            val file = FlipperFile(FlipperFilePath("nfc", "test.nfc"), FlipperKeyContent.RawData("old".toByteArray()))
            val key = FlipperKey(file, notes = "old notes", synchronized = true, deleted = false)
            keys.insertKey(key)
            val uid = database.keyDao().getByPath("nfc/test.nfc", false)!!.uid
            val restored = key.copy(mainFile = file.copy(content = FlipperKeyContent.RawData("restored".toByteArray())), notes = "restored notes")
            keys.restoreKeys(listOf(restored))
            assertEquals(uid, database.keyDao().getByPath("nfc/test.nfc", false)!!.uid)
            val result = keys.getKey(key.getKeyPath())!!
            assertEquals("restored notes", result.notes)
            assertEquals("restored", result.keyContent.openStream().readBytes().decodeToString())
            assertFalse(result.synchronized)
            assertEquals(1, keys.getAllKeys().size)
        } finally { database.close() }
    }

    @Test
    fun `a failed key restore rolls back preceding updates`() = runTest {
        val database = database()
        try {
            val keys = SimpleKeyApiImpl({ database.keyDao() }, { database.additionalFileDao() }, { database })
            val file = FlipperFile(FlipperFilePath("nfc", "test.nfc"), FlipperKeyContent.RawData("original".toByteArray()))
            val key = FlipperKey(file, notes = "original", synchronized = true, deleted = false)
            keys.insertKey(key)
            val extra = FlipperFile(FlipperFilePath("nfc", "other.shd"), FlipperKeyContent.RawData(byteArrayOf(1)))
            val invalid = key.copy(mainFile = file.copy(path = FlipperFilePath("nfc", "other.nfc")), additionalFiles = persistentListOf(extra, extra))
            val failure = runCatching { keys.restoreKeys(listOf(key.copy(notes = "changed"), invalid)) }.exceptionOrNull()
            assertNotNull(failure)
            assertEquals("original", keys.getKey(key.getKeyPath())!!.notes)
            assertEquals(1, keys.getAllKeys().size)
        } finally { database.close() }
    }
}
