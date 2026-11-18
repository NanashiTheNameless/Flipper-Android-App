package dev.namelessnanashi.flipperzero.updater.subghz.tasks

import android.os.Build
import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.RegionApi.Companion.REGION_FILE
import dev.namelessnanashi.flipperzero.core.test.readTestAsset
import dev.namelessnanashi.flipperzero.updater.api.DownloaderApi
import dev.namelessnanashi.flipperzero.updater.downloader.api.DownloaderApiImpl
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.RegionProvisioningHelper
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SkipProvisioningHelper
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SubGhzProvisioningHelper
import dev.namelessnanashi.flipperzero.updater.subghz.helpers.SubGhzProvisioningHelperImpl
import dev.namelessnanashi.flipperzero.updater.subghz.model.RegionProvisioning
import io.ktor.client.HttpClient
import io.ktor.client.engine.mock.MockEngine
import io.ktor.client.engine.mock.respond
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpStatusCode
import io.ktor.http.headersOf
import io.ktor.serialization.kotlinx.json.json
import io.mockk.coEvery
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import okio.sink
import org.junit.Assert
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.ParameterizedRobolectricTestRunner
import org.robolectric.annotation.Config
import java.io.ByteArrayOutputStream

@RunWith(ParameterizedRobolectricTestRunner::class)
@Config(sdk = [Build.VERSION_CODES.TIRAMISU])
class SubGhzProvisioningHelperTest(
    private val countryName: String?
) {
    private lateinit var downloaderApi: DownloaderApi
    private lateinit var regionProvisioningHelper: RegionProvisioningHelper
    private lateinit var skipProvisioningHelper: SkipProvisioningHelper
    private lateinit var underTest: SubGhzProvisioningHelper

    @Before
    fun setUp() = runTest {
        val mockEngine = MockEngine { _ ->
            respond(
                content = readTestAsset("regions.json"),
                status = HttpStatusCode.OK,
                headers = headersOf(HttpHeaders.ContentType, "application/json")
            )
        }
        val client = HttpClient(mockEngine) {
            install(ContentNegotiation) {
                json()
            }
        }
        downloaderApi = DownloaderApiImpl(
            client = client,
            downloadAndUnpackDelegateApi = mockk(),
            storageProvider = mockk()
        )
        regionProvisioningHelper = mockk()

        coEvery { regionProvisioningHelper.provideRegion(eq("TT")) } returns RegionProvisioning(
            regionFromNetwork = countryName,
            regionFromSim = null,
            regionFromIp = null,
            regionSystem = null,
            isRoaming = false
        )

        skipProvisioningHelper = mockk {
            coEvery { shouldSkipProvisioning(any()) } returns false
        }
        underTest = SubGhzProvisioningHelperImpl(
            downloaderApi,
            regionProvisioningHelper,
            skipProvisioningHelper
        )
    }

    @Test
    fun `check region protobuf`() = runTest {
        val fGetInfoFeatureApi = mockk<FGetInfoFeatureApi>()
        val ostream = ByteArrayOutputStream()
        val fFileUploadApi = mockk<FFileUploadApi> {
            coEvery {
                sink(REGION_FILE)
            } coAnswers {
                ostream.sink()
            }
        }

        underTest.provideAndUploadSubGhz(
            fGetInfoFeatureApi,
            fFileUploadApi
        )

        val expectedBytes = readTestAsset("regions/region_data_${countryName ?: "WW"}")

        val sendBytes = ostream.toByteArray()
        Assert.assertTrue(expectedBytes.contentEquals(sendBytes))
    }

    @Test
    fun `Get region`() = runTest {
        Assert.assertEquals(underTest.getRegion(), countryName)
    }

    companion object {
        @JvmStatic
        @ParameterizedRobolectricTestRunner.Parameters
        fun data() = listOf(
            "AL", "AM", "AT", "AU", "AZ", "BA", "BE", "BG", "BY", "CA", "CH", "CY",
            "CZ", "DE", "DK", "DZ", "EE", "EG", "ES", "FI", "FR", "GB", "GE", "GR",
            "HR", "HU", "IE", "IL", "IS", "IT", "JO", "JP", "LB", "LI", "LT", "LU",
            "LV", "LY", "MA", "MD", "ME", "MK", "MT", "NL", "NO", "PL", "PS", "PT",
            "RO", "RS", "RU", "SE", "SI", "SK", "SY", "TN", "TR", "UA", "US", "XK",
            "WW", null
        )
    }
}
