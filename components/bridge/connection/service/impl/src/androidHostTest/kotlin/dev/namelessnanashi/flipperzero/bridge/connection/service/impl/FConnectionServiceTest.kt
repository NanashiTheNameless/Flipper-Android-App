package dev.namelessnanashi.flipperzero.bridge.connection.service.impl

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.FDevicePersistedStorage
import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroUsbModel
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.DisconnectStatus
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.test.advanceTimeBy
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Test

class FConnectionServiceTest {
    @Test
    fun `manual disconnect can reconnect and repeated connect always retries`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val device = FDeviceFlipperZeroUsbModel("USB", "/dev/bus/usb/002/002")
            val orchestrator = mockk<FDeviceOrchestrator>(relaxed = true) {
                every { getState() } returns MutableStateFlow(
                    FDeviceConnectStatus.Disconnected(device, DisconnectStatus.NOT_INITIALIZED)
                )
            }
            val storage = mockk<FDevicePersistedStorage> {
                every { getCurrentDevice() } returns MutableStateFlow(device)
            }
            val service = FConnectionServiceImpl(orchestrator, storage)
            service.onApplicationInit()
            runCurrent()
            coVerify(exactly = 1) { orchestrator.connect(device) }

            service.disconnect(force = true)
            runCurrent()
            coVerify(exactly = 1) { orchestrator.disconnectCurrent() }
            coVerify(exactly = 1) { orchestrator.connect(device) }

            service.forceReconnect()
            runCurrent()
            coVerify(exactly = 2) { orchestrator.connect(device) }

            service.forceReconnect()
            runCurrent()
            coVerify(exactly = 3) { orchestrator.connect(device) }
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }
    @Test
    fun `unplugged usb selection is never retried`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val oldDevice = FDeviceFlipperZeroUsbModel("USB", "/dev/bus/usb/002/002")
            val orchestrator = mockk<FDeviceOrchestrator>(relaxed = true) {
                every { getState() } returns MutableStateFlow(
                    FDeviceConnectStatus.Disconnected(oldDevice, DisconnectStatus.ERROR_UNKNOWN)
                )
            }
            val storage = mockk<FDevicePersistedStorage> {
                every { getCurrentDevice() } returns MutableStateFlow(null)
            }
            FConnectionServiceImpl(orchestrator, storage).onApplicationInit()
            runCurrent()
            advanceTimeBy(2000)
            runCurrent()

            coVerify(exactly = 0) { orchestrator.connect(any()) }
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }

}
