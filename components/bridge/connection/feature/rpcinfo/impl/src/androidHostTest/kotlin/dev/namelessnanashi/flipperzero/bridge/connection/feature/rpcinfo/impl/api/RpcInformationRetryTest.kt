package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.api.FRpcFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.model.FlipperRequest
import dev.namelessnanashi.flipperzero.bridge.connection.feature.rpcinfo.api.awaitRpcInformation
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.awaitCancellation
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.*
import org.junit.Test

class RpcInformationRetryTest {
    @Test
    fun `timed out information load can be retried without forcing or reconnecting`() = runTest {
        var requests = 0
        val rpc = mockk<FRpcFeatureApi> {
            every { request(any<FlipperRequest>()) } returns flow {
                requests++
                if (requests == 1) awaitCancellation()
            }
        }
        val feature = FRpcInfoFeatureApiImpl(rpc, null)
        assertTrue(runCatching { feature.awaitRpcInformation(100) }.exceptionOrNull() is IllegalStateException)
        assertEquals(1, requests)
        feature.awaitRpcInformation(100)
        assertEquals(3, requests) // Retried device information, followed by power information.
        feature.awaitRpcInformation(100)
        assertEquals(3, requests) // Completed information is reused.
    }
}
