package dev.namelessnanashi.flipperzero.faphub.source.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.FRpcInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.awaitRpcInformation
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FirmwareInfo
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperDeviceInfo
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperInformationStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.model.FlipperRpcInformation
import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.async
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.test.runCurrent
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

@OptIn(kotlinx.coroutines.ExperimentalCoroutinesApi::class)
class DeviceReadinessTest {
    private val information = FlipperRpcInformation(
        flipperDeviceInfo = FlipperDeviceInfo(serialNumber = "serial-1"),
        firmware = FirmwareInfo(softwareRevision = "mntm-009")
    )
    private val state = MutableStateFlow<FlipperInformationStatus<FlipperRpcInformation>>(
        FlipperInformationStatus.NotStarted()
    )
    private val feature = mockk<FRpcInfoFeatureApi> {
        every { getRpcInformationFlow() } returns state
        coEvery { invalidate(any(), any()) } returns Unit
    }
    private val featureState = MutableStateFlow<FFeatureStatus<FRpcInfoFeatureApi>>(
        FFeatureStatus.Supported(feature)
    )
    private val targetState = MutableStateFlow<FlipperTarget?>(FlipperTarget.Received("f7", SemVer(88, 2)))
    private val features = mockk<FFeatureProvider> {
        every { get(FRpcInfoFeatureApi::class) } returns featureState
    }
    private val targets = mockk<FlipperTargetProviderApi> {
        every { getFlipperTarget() } returns targetState
    }

    private fun jobs() = SourceJobs(mockk(), mockk(), features, targets, mockk())

    @Test
    fun `source operation starts information loading without visiting device info screen`() = runTest {
        coEvery { feature.invalidate(any(), false) } coAnswers {
            firstArg<kotlinx.coroutines.CoroutineScope>().launch {
                state.value = FlipperInformationStatus.InProgress(information)
                delay(100)
                state.value = FlipperInformationStatus.Ready(information)
            }
            Unit
        }
        assertEquals(targetState.value to information, jobs().device())
        coVerify(exactly = 1) { feature.invalidate(any(), false) }
    }

    @Test
    fun `in flight information is awaited even when partial identity is already present`() = runTest {
        state.value = FlipperInformationStatus.InProgress(information)
        val result = async { jobs().device() }
        runCurrent()
        assertFalse(result.isCompleted)
        state.value = FlipperInformationStatus.Ready(information)
        assertEquals(information, result.await().second)
        coVerify(exactly = 1) { feature.invalidate(any(), false) }
    }

    @Test
    fun `already loaded information can be used immediately`() = runTest {
        state.value = FlipperInformationStatus.Ready(information)
        assertEquals(information, jobs().device().second)
    }

    @Test
    fun `stalled information fails with connection guidance instead of worker timeout`() = runTest {
        val result = runCatching { feature.awaitRpcInformation(timeoutMillis = 100) }
        assertTrue(result.exceptionOrNull() is IllegalStateException)
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("Check the Flipper connection"))
    }

    @Test
    fun `waiting can be cancelled`() = runTest {
        val result = async { feature.awaitRpcInformation() }
        runCurrent()
        result.cancelAndJoin()
        assertTrue(result.isCancelled)
    }

    @Test
    fun `device change during loading cannot be used for an installation`() = runTest {
        val result = async { runCatching { jobs().device() } }
        runCurrent()
        featureState.value = FFeatureStatus.Supported(mockk())
        state.value = FlipperInformationStatus.Ready(information)
        assertTrue(result.await().exceptionOrNull()?.message.orEmpty().contains("connection changed"))
    }

    @Test
    fun `disconnect during loading cannot be used for an installation`() = runTest {
        val result = async { runCatching { jobs().device() } }
        runCurrent()
        targetState.value = FlipperTarget.NotConnected
        featureState.value = FFeatureStatus.Retrieving
        state.value = FlipperInformationStatus.Ready(information)
        assertTrue(result.await().isFailure)
    }

    @Test
    fun `finished information still requires identity and firmware`() = runTest {
        state.value = FlipperInformationStatus.Ready(FlipperRpcInformation())
        val result = runCatching { jobs().device() }
        assertTrue(result.exceptionOrNull()?.message.orEmpty().contains("identity and firmware"))
    }
}
