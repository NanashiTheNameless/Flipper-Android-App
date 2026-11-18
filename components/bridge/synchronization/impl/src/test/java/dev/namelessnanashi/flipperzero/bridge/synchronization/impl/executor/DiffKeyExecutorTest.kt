package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.DiffSource
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyAction
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyDiff
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyWithHash
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Test
import java.util.Collections

class DiffKeyExecutorTest {
    private class FakeStorage(
        private val failOn: Set<String> = emptySet()
    ) : AbstractKeyStorage {
        val loaded: MutableList<String> = Collections.synchronizedList(mutableListOf())
        val saved = mutableListOf<String>()
        val deleted = mutableListOf<String>()

        override suspend fun loadFile(filePath: FlipperFilePath): FlipperKeyContent {
            loaded += filePath.nameWithExtension
            if (filePath.nameWithExtension in failOn) error("Failed load")
            return FlipperKeyContent.RawData(filePath.nameWithExtension.encodeToByteArray())
        }

        override suspend fun modify(filePath: FlipperFilePath, newContent: FlipperKeyContent) {
            saved += filePath.nameWithExtension
        }

        override suspend fun saveFile(filePath: FlipperFilePath, keyContent: FlipperKeyContent) {
            saved += filePath.nameWithExtension
        }

        override suspend fun deleteFile(filePath: FlipperFilePath) {
            deleted += filePath.nameWithExtension
        }
    }

    private fun diff(name: String, action: KeyAction) = KeyDiff(
        newHash = KeyWithHash(FlipperFilePath("subghz", name), hash = name),
        action = action,
        source = DiffSource.FLIPPER
    )

    private val tracker = DetailedProgressWrapperTracker(progressListener = mockk(relaxed = true))

    @Test
    fun `saves every file in order`() = runTest {
        val source = FakeStorage()
        val target = FakeStorage()
        val diffs = (0 until 10).map { diff("key$it.sub", KeyAction.ADD) }

        val applied = DiffKeyExecutorImpl().executeBatch(source, target, diffs, tracker)

        Assert.assertEquals(diffs, applied)
        Assert.assertEquals(diffs.map { it.newHash.keyPath.nameWithExtension }, target.saved)
    }

    @Test
    fun `failed load skips only that file`() = runTest {
        val source = FakeStorage(failOn = setOf("key1.sub"))
        val target = FakeStorage()
        val diffs = (0 until 3).map { diff("key$it.sub", KeyAction.ADD) }

        val applied = DiffKeyExecutorImpl().executeBatch(source, target, diffs, tracker)

        Assert.assertEquals(listOf("key0.sub", "key2.sub"), applied.map { it.newHash.keyPath.nameWithExtension })
        Assert.assertEquals(listOf("key0.sub", "key2.sub"), target.saved)
    }

    @Test
    fun `delete does not load from source`() = runTest {
        val source = FakeStorage()
        val target = FakeStorage()

        DiffKeyExecutorImpl().executeBatch(
            source,
            target,
            listOf(diff("old.sub", KeyAction.DELETED)),
            tracker
        )

        Assert.assertTrue(source.loaded.isEmpty())
        Assert.assertEquals(listOf("old.sub"), target.deleted)
    }
}
