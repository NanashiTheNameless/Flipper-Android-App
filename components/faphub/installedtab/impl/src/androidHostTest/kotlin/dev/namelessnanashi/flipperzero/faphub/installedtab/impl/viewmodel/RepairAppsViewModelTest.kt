package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairApi
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairProgress
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairReport
import dev.namelessnanashi.flipperzero.faphub.installation.all.api.FapRepairStage
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.CompletableDeferred
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Test

class RepairAppsViewModelTest {
    @Test
    fun `check reports app and download progress and can be cancelled`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val gate = CompletableDeferred<Unit>()
            val progress = FapRepairProgress(2, 10, "Test app", FapRepairStage.DOWNLOADING, 0.5f)
            val api = mockk<FapRepairApi> {
                coEvery { check(any()) } coAnswers {
                    firstArg<suspend (FapRepairProgress) -> Unit>()(progress)
                    gate.await()
                    FapRepairReport(10, emptyList(), emptyList())
                }
            }
            val vm = RepairAppsViewModel(api)
            vm.onCheck()
            runCurrent()
            assertEquals(progress, (vm.getState().value as RepairAppsState.Checking).progress)
            assertEquals(0.25f, progress.fraction, 0.001f)

            vm.onCancel()
            runCurrent()
            gate.complete(Unit)
            runCurrent()
            assertEquals(RepairAppsState.Idle, vm.getState().value)
            vm.onDestroy()
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }
}
