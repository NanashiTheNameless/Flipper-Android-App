package dev.namelessnanashi.flipperzero.updater.subghz.helpers

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGetInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.update.api.RegionApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.copyWithProgress
import com.flipperdevices.protobuf.Region
import dev.namelessnanashi.flipperzero.updater.api.DownloaderApi
import dev.namelessnanashi.flipperzero.updater.subghz.model.FailedUploadSubGhzException
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import okio.ByteString.Companion.encode
import okio.buffer
import okio.source
import java.nio.charset.Charset
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

interface SubGhzProvisioningHelper {
    suspend fun provideAndUploadSubGhz(
        fGetInfoFeatureApi: FGetInfoFeatureApi,
        fFileUploadApi: FFileUploadApi
    )

    suspend fun getRegion(): String?
}

@ContributesBinding(AppGraph::class, binding<SubGhzProvisioningHelper>())
class SubGhzProvisioningHelperImpl @Inject constructor(
    private val downloaderApi: DownloaderApi,
    private val regionProvisioningHelper: RegionProvisioningHelper,
    private val skipProvisioningHelper: SkipProvisioningHelper
) : SubGhzProvisioningHelper, LogTagProvider {
    override val TAG = "SubGhzProvisioningHelper"

    override suspend fun getRegion(): String? {
        val response = downloaderApi.getSubGhzProvisioning()
        val providedRegions = regionProvisioningHelper.provideRegion(
            response.country?.uppercase()
        )
        return providedRegions.provideRegion().first?.uppercase()
    }

    override suspend fun provideAndUploadSubGhz(
        fGetInfoFeatureApi: FGetInfoFeatureApi,
        fFileUploadApi: FFileUploadApi
    ) = withContext(FlipperDispatchers.workStealingDispatcher) {
        if (skipProvisioningHelper.shouldSkipProvisioning(fGetInfoFeatureApi)) {
            info { "Skip provisioning" }
            return@withContext
        }

        val response = downloaderApi.getSubGhzProvisioning()
        val providedRegions = regionProvisioningHelper.provideRegion(
            response.country?.uppercase()
        )
        val (providedRegion, _) = providedRegions.provideRegion()
        val providedBands = if (providedRegion != null) {
            response.countries[providedRegion.uppercase()] ?: response.defaults
        } else {
            response.defaults
        }

        val finalCodeRegion = providedRegion?.uppercase() ?: RegionApi.UNKNOWN_REGION

        val regionData = Region(
            country_code = finalCodeRegion.encode(Charset.forName("ASCII")),
            bands = providedBands.map {
                Region.Band(
                    start = it.start.toInt(),
                    end = it.end.toInt(),
                    power_limit = it.maxPower,
                    duty_cycle = it.dutyCycle.toInt()
                )
            }
        ).encode()
        try {
            regionData.inputStream().source().buffer().use { bufferedSource ->
                fFileUploadApi.sink(RegionApi.REGION_FILE).use { sink ->
                    bufferedSource.copyWithProgress(
                        sink = sink,
                        progressListener = { _, _ -> },
                        sourceLength = { regionData.size.toLong() }
                    )
                }
            }
        } catch (e: Exception) {
            error(e) { "#provideAndUploadSubGhz unknown exception" }
            throw FailedUploadSubGhzException()
        }
    }
}
