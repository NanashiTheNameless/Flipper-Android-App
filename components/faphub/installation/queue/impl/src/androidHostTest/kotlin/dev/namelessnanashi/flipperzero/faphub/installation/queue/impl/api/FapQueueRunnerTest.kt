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
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class FapQueueRunnerTest {
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
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }
}
