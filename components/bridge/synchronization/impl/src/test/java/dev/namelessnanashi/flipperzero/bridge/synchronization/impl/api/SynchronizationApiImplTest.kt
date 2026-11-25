package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.api

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationState
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.SynchronizationTask
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.TestScope
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SynchronizationApiImplTest {
    @Test
    fun `requests after completion wait five seconds and merge into one sync`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        builder.tasks.single().finish()
        assertEquals(SynchronizationState.Finished, api.getSynchronizationState().value)
        assertFalse(api.isSynchronizationRunning())

        advanceTimeBy(2_000)
        repeat(3) { api.startSynchronization(force = true) }
        advanceTimeBy(2_999)
        runCurrent()
        assertEquals(1, builder.tasks.size)

        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, builder.tasks.size)
        assertTrue(api.isSynchronizationRunning())
    }

    @Test
    fun `dirty sync waits after completion rather than after its start`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        api.startSynchronization(force = true)
        advanceTimeBy(10_000)
        builder.tasks.single().finish()

        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(1, builder.tasks.size)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(2, builder.tasks.size)
    }

    @Test
    fun `automatic request during cooldown also waits`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        builder.tasks.single().finish()
        api.startSynchronization()
        assertEquals(1, builder.tasks.size)
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(2, builder.tasks.size)
    }

    @Test
    fun `cooldown expires without starting an unrequested sync`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        builder.tasks.single().finish()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, builder.tasks.size)
        api.startSynchronization(force = true)
        assertEquals(2, builder.tasks.size)
    }

    @Test
    fun `stop clears a queued restart and still enforces cooldown`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        api.startSynchronization(force = true)
        api.stop()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, builder.tasks.size)

        api.startSynchronization(force = true)
        api.stop()
        api.startSynchronization(force = true)
        advanceTimeBy(4_999)
        runCurrent()
        assertEquals(2, builder.tasks.size)
        advanceTimeBy(1)
        runCurrent()
        assertEquals(3, builder.tasks.size)
    }

    @Test
    fun `stop during cooldown clears pending requests`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        builder.tasks.single().finish()
        api.startSynchronization(force = true)
        api.stop()
        advanceTimeBy(5_000)
        runCurrent()
        assertEquals(1, builder.tasks.size)
    }

    @Test
    fun `late callbacks from an old task cannot finish a new task`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder)
        api.startSynchronization(force = true)
        val oldTask = builder.tasks.single()
        oldTask.finish()
        advanceTimeBy(5_000)
        runCurrent()
        api.startSynchronization(force = true)
        oldTask.finish()
        assertTrue(api.isSynchronizationRunning())
        assertEquals(2, builder.tasks.size)
    }

    @Test
    fun `skipping automatic debug sync does not prevent a manual sync`() = runTest {
        val builder = TestTaskBuilder()
        val api = createApi(builder, skipAutoSync = true)
        api.startSynchronization()
        if (BuildKonfig.CRASH_APP_ON_FAILED_CHECKS) {
            assertFalse(api.isSynchronizationRunning())
            assertTrue(builder.tasks.isEmpty())
        } else {
            builder.tasks.single().finish()
            advanceTimeBy(5_000)
            runCurrent()
        }
        api.startSynchronization(force = true)
        assertTrue(api.isSynchronizationRunning())
    }

    private fun TestScope.createApi(builder: TestTaskBuilder, skipAutoSync: Boolean = false): SynchronizationApiImpl {
        val store = mockk<DataStore<Settings>> {
            every { data } returns flowOf(Settings(skip_auto_sync_in_debug = skipAutoSync))
        }
        return SynchronizationApiImpl(builder, store, this)
    }

    private class TestTaskBuilder : SynchronizationTask.Builder {
        val tasks = mutableListOf<TestTask>()
        override fun build() = TestTask().also { tasks.add(it) }
    }

    private class TestTask : SynchronizationTask {
        private lateinit var listener: suspend (SynchronizationState) -> Unit

        override fun start(input: Unit, stateListener: suspend (SynchronizationState) -> Unit): Job {
            listener = stateListener
            return Job()
        }

        suspend fun finish() = listener(SynchronizationState.Finished)

        // Real tasks report Finished again from their lifecycle cleanup.
        override suspend fun onStop() = finish()
    }
}
