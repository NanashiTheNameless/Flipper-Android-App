package dev.namelessnanashi.flipperzero.updater.card.helpers

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.updater.card.helpers.delegates.UpdateOfferDebugFlagAlways
import io.mockk.every
import io.mockk.mockk
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class UpdateOfferProviderTest {
    private val delegateFlagAlways: UpdateOfferDebugFlagAlways = mockk()
    private val fStorageFeatureApi: FStorageFeatureApi = mockk()
    private val updateOfferHelper = UpdateOfferProvider(
        delegates = mutableSetOf(delegateFlagAlways)
    )

    @Before
    fun setup() {
        every { delegateFlagAlways.isRequire(fStorageFeatureApi) } returns flowOf(false)
    }

    @Test
    fun `Update not offer`() = runTest {
        updateOfferHelper.isUpdateRequire(fStorageFeatureApi).collect {
            Assert.assertFalse(it)
        }
    }

    @Test
    fun `Always update in setting`() = runTest {
        every { delegateFlagAlways.isRequire(fStorageFeatureApi) } returns flowOf(true)
        updateOfferHelper.isUpdateRequire(fStorageFeatureApi).collect {
            Assert.assertTrue(it)
        }
    }
}
