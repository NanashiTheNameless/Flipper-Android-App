package dev.namelessnanashi.flipperzero.updater.subghz.tasks

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.model.FGetInfoApiProperty
import dev.namelessnanashi.flipperzero.core.ktx.jre.TimeHelper
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SkipProvisioningHelper
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SkipProvisioningHelperImpl
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkObject
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.test.runTest
import org.junit.Assert
import org.junit.Before
import org.junit.Test

class SkipProvisioningHelperTest {
    private lateinit var settings: DataStore<Settings>
    private lateinit var underTest: SkipProvisioningHelper

    @Before
    fun setUp() {
        settings = mockk()
        underTest = SkipProvisioningHelperImpl(settings)

        mockkObject(TimeHelper)
        every { TimeHelper.getNanoTime() } returns 0L
    }

    @Test
    fun `not skip provisioning if settings disable`() = runTest {
        every { settings.data } returns flowOf(
            Settings(
                ignore_subghz_provisioning_on_zero_region = false
            )
        )

        val shouldProvide = underTest.shouldSkipProvisioning(mockk())

        Assert.assertFalse(shouldProvide)
    }

    @Test
    fun `not skip provisioning if hardware region null`() = runTest {
        every { settings.data } returns flowOf(
            Settings(
                ignore_subghz_provisioning_on_zero_region = true
            )
        )

        val featureApi = mockk<FGetInfoFeatureApi> {
            coEvery {
                get(FGetInfoApiProperty.DeviceInfo.HARDWARE_REGION)
            } returns Result.failure(Throwable("Test error no region"))
        }

        val shouldProvide = underTest.shouldSkipProvisioning(
            featureApi
        )

        Assert.assertFalse(shouldProvide)
    }

    @Test
    fun `not skip provisioning if hardware region not zero`() = runTest {
        every { settings.data } returns flowOf(
            Settings(
                ignore_subghz_provisioning_on_zero_region = true
            )
        )

        val featureApi = mockk<FGetInfoFeatureApi> {
            coEvery {
                get(FGetInfoApiProperty.DeviceInfo.HARDWARE_REGION)
            } returns Result.success("1")
        }

        val shouldProvide = underTest.shouldSkipProvisioning(
            featureApi
        )

        Assert.assertFalse(shouldProvide)
    }

    @Test
    fun `skip provisioning`() = runTest {
        every { settings.data } returns flowOf(
            Settings(
                ignore_subghz_provisioning_on_zero_region = true
            )
        )

        val featureApi = mockk<FGetInfoFeatureApi> {
            coEvery {
                get(FGetInfoApiProperty.DeviceInfo.HARDWARE_REGION)
            } returns Result.success("0")
        }

        val shouldProvide = underTest.shouldSkipProvisioning(
            featureApi
        )

        Assert.assertTrue(shouldProvide)
    }
}
