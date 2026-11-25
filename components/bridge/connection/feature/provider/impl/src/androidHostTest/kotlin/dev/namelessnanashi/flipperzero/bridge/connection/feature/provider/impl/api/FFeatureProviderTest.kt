package dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.config.api.model.FDeviceFlipperZeroUsbModel
import dev.namelessnanashi.flipperzero.bridge.connection.device.common.api.FDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.impl.utils.FDeviceConnectStatusToDeviceApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import io.mockk.unmockkObject
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.test.StandardTestDispatcher
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertSame
import org.junit.Test

class FFeatureProviderTest {
    @Test
    fun `reconnect cancels initialization on the old device`() = runTest {
        mockkObject(FlipperDispatchers)
        try {
            every { FlipperDispatchers.workStealingDispatcher } returns StandardTestDispatcher(testScheduler)
            val device = FDeviceFlipperZeroUsbModel("USB", "/dev/bus/usb/002/002")
            val first = FDeviceConnectStatus.Connected(backgroundScope, device, mockk())
            val second = FDeviceConnectStatus.Connected(backgroundScope, device, mockk())
            val state = MutableStateFlow<FDeviceConnectStatus>(first)
            val orchestrator = mockk<FDeviceOrchestrator> { every { getState() } returns state }
            val oldDevice = mockk<FDeviceApi> {
                coEvery { get(FRpcFeatureApi::class) } coAnswers { awaitCancellation() }
            }
            val feature = mockk<FRpcFeatureApi>()
            val newDevice = mockk<FDeviceApi> { coEvery { get(FRpcFeatureApi::class) } returns feature }
            val mapper = mockk<FDeviceConnectStatusToDeviceApi> {
                every { get(first) } returns oldDevice
                every { get(second) } returns newDevice
            }
            val provider = FFeatureProviderImpl(orchestrator, mapper)
            var latest: FFeatureStatus<FRpcFeatureApi>? = null
            provider.get(FRpcFeatureApi::class).onEach { latest = it }.launchIn(backgroundScope)
            runCurrent()

            state.value = second
            runCurrent()

            assertSame(feature, (latest as FFeatureStatus.Supported).featureApi)
        } finally {
            unmockkObject(FlipperDispatchers)
        }
    }
}
