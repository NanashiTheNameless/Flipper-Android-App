package dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.api

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.faphub.dao.api.FapDownloadApi
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapBuildState
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemVersion
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapActionRequest
import dev.namelessnanashi.flipperzero.faphub.installation.queue.impl.executor.FapActionExecutor
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import java.io.File
import dev.namelessnanashi.flipperzero.core.progress.ProgressListener
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FapQueueState
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.async
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.cancelAndJoin
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.model.ExternalAppSource
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FapQueueRunnerTest {
    private fun artifactRequest() = FapActionRequest.InstallArtifact(
        "external:" + "a".repeat(64), "/cache/app.fap", "app", "Tools",
        ExternalAppSource("https://example.com/app.fap", "", "file", "b".repeat(64)),
        FlipperTarget.Received("f7", SemVer(88, 2)), "serial", "firmware"
    )

    @Test
    fun `artifact completion waits for the common upload queue without catalog downloads`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val gate = CompletableDeferred<Unit>()
            val executor = mockk<FapActionExecutor> { coEvery { execute(any(), any()) } coAnswers { gate.await() } }
            val downloader = mockk<FapDownloadApi>(relaxed = true)
            val runner = FapQueueRunner(executor, downloader)
            val result = backgroundScope.async { runner.enqueueAndAwait(artifactRequest()) }
            runCurrent()
            assertEquals(false, result.isCompleted)
            coVerify(exactly = 1) { executor.execute(any(), any()) }
            coVerify(exactly = 0) { downloader.downloadBundle(any(), any(), any()) }
            gate.complete(Unit)
            runCurrent()
            assertEquals(true, result.isCompleted)
            result.await()
        } finally { unmockkObject(FlipperDispatchers) }
    }

    @Test
    fun `cancelled artifact waiters also cancel their device operation`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val gate = CompletableDeferred<Unit>()
            var cancelled = false
            val executor = mockk<FapActionExecutor> {
                coEvery { execute(any(), any()) } coAnswers {
                    try { gate.await() } catch (error: CancellationException) { cancelled = true; throw error }
                }
            }
            val runner = FapQueueRunner(executor, mockk(relaxed = true))
            val result = backgroundScope.async { runner.enqueueAndAwait(artifactRequest()) }
            runCurrent()
            result.cancel()
            runCurrent()
            assertEquals(true, cancelled)
            assertEquals(null, runner.currentTaskFlow().value)
        } finally { unmockkObject(FlipperDispatchers) }
    }

    @Test
    fun `eight bundles download concurrently while only one app installs`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val installGate = CompletableDeferred<Unit>()
            val downloadGate = CompletableDeferred<Unit>()
            val executor = mockk<FapActionExecutor> {
                coEvery { execute(any(), any()) } coAnswers { installGate.await() }
            }
            var active = 0
            var peak = 0
            var completed = 0
            val downloader = mockk<FapDownloadApi> {
                coEvery { discardBundle(any(), any()) } returns Unit
                coEvery { downloadBundle(any(), any(), any()) } coAnswers {
                    thirdArg<ProgressListener>().onProgress(0.42f)
                    active++
                    peak = maxOf(peak, active)
                    try {
                        downloadGate.await()
                        completed++
                        File("cached.fap")
                    } finally {
                        active--
                    }
                }
            }
            val runner = FapQueueRunner(executor, downloader)
            val sdk = SemVer(1, 0)
            repeat(12) { index ->
                runner.enqueueSync(FapActionRequest.Install(
                    applicationAlias = "app$index",
                    applicationUid = "app$index",
                    applicationName = "App $index",
                    toVersion = FapItemVersion("version$index", sdk, FlipperTarget.Received("f7", sdk), FapBuildState.READY, sdk),
                    categoryAlias = "tools",
                    iconUrl = ""
                ))
            }
            var queuedState: FapQueueState? = null
            FapInstallationQueueApiImpl(runner).getFlowById("app5")
                .onEach { queuedState = it }.launchIn(backgroundScope)
            runCurrent()

            assertEquals(0.42f, (queuedState as FapQueueState.Pending).downloadProgress, 0.001f)
            assertEquals(8, active)
            coVerify(exactly = 1) { executor.execute(any(), any()) }

            downloadGate.complete(Unit)
            runCurrent()
            assertEquals(1f, (queuedState as FapQueueState.Pending).downloadProgress, 0.001f)
            assertEquals(8, peak)
            assertEquals(11, completed)
            coVerify(exactly = 1) { executor.execute(any(), any()) }

            installGate.complete(Unit)
            runCurrent()
            coVerify(exactly = 12) { executor.execute(any(), any()) }
            coVerify(exactly = 12) { downloader.discardBundle(any(), any()) }
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }

    @Test
    fun `cancelled prefetch discards its bundle once the active download finishes`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val installing = CompletableDeferred<Unit>()
            val downloading = CompletableDeferred<Unit>()
            val executor = mockk<FapActionExecutor> { coEvery { execute(any(), any()) } coAnswers { installing.await() } }
            val downloader = mockk<FapDownloadApi>(relaxed = true) {
                coEvery { downloadBundle(any(), "version1", any()) } coAnswers { downloading.await(); File("cache.fap") }
            }
            val runner = FapQueueRunner(executor, downloader)
            val sdk = SemVer(87, 1)
            val target = FlipperTarget.Received("f7", sdk)
            repeat(2) { index ->
                runner.enqueueSync(FapActionRequest.Install("app$index", "app$index", "App $index",
                    FapItemVersion("version$index", sdk, target, FapBuildState.READY, sdk), "Tools", ""))
            }
            runCurrent()
            runner.enqueueSync(FapActionRequest.Cancel("app1"))
            downloading.complete(Unit)
            runCurrent()
            coVerify(exactly = 1) { downloader.discardBundle(target, "version1") }
            runner.enqueueSync(FapActionRequest.Cancel("app0"))
        } finally { unmockkObject(FlipperDispatchers) }
    }
}
