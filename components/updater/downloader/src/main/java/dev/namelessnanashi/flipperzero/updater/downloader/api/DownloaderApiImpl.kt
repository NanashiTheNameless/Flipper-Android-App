package dev.namelessnanashi.flipperzero.updater.downloader.api

import dev.namelessnanashi.flipperzero.core.FlipperStorageProvider
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.log.verbose
import dev.namelessnanashi.flipperzero.updater.api.DownloadAndUnpackDelegateApi
import dev.namelessnanashi.flipperzero.updater.api.DownloaderApi
import dev.namelessnanashi.flipperzero.updater.downloader.model.ArtifactType
import dev.namelessnanashi.flipperzero.updater.downloader.model.FirmwareDirectoryListeningResponse
import dev.namelessnanashi.flipperzero.updater.downloader.model.SubGhzProvisioningResponse
import dev.namelessnanashi.flipperzero.updater.downloader.model.Target
import dev.namelessnanashi.flipperzero.updater.model.DistributionFile
import dev.namelessnanashi.flipperzero.updater.model.DownloadProgress
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.SubGhzProvisioningException
import dev.namelessnanashi.flipperzero.updater.model.SubGhzProvisioningModel
import dev.namelessnanashi.flipperzero.updater.model.VersionFiles
import dev.zacsweers.metro.ContributesBinding
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.get
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.channelFlow
import java.io.File
import java.util.EnumMap
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

// Momentum firmware; same directory schema as update.flipperzero.one
private const val UPDATER_URL = "https://up.momentum-fw.dev/firmware/directory.json"
private const val SUB_GHZ_URL = "https://update.flipperzero.one/regions/api/v0/bundle"

@ContributesBinding(AppGraph::class, binding<DownloaderApi>())
class DownloaderApiImpl @Inject constructor(
    private val client: HttpClient,
    private val downloadAndUnpackDelegateApi: DownloadAndUnpackDelegateApi,
    private val storageProvider: FlipperStorageProvider
) : DownloaderApi, LogTagProvider {
    override val TAG = "DownloaderApi"

    override suspend fun getLatestVersion(): EnumMap<FirmwareChannel, VersionFiles> {
        val versionMap: EnumMap<FirmwareChannel, VersionFiles> =
            EnumMap(FirmwareChannel::class.java)

        val response = client.get(
            urlString = UPDATER_URL
        ).body<FirmwareDirectoryListeningResponse>()

        verbose { "Receive response from server" }

        response.channels.map { channel ->
            channel.id to channel.versions.maxByOrNull { it.timestamp }
        }.mapNotNull { (channel, versions) ->
            if (channel == null || versions == null) {
                null
            } else {
                channel to versions
            }
        }.forEach { (channel, version) ->
            val updaterFile = version
                .files
                .filter { it.type == ArtifactType.UPDATE_TGZ }
                .find { it.target == Target.F7 }
                ?: return@forEach

            versionMap[channel.original] = VersionFiles(
                version = FirmwareVersion(
                    channel.original,
                    version.version.clearVersion()
                ),
                updaterFile = DistributionFile(
                    updaterFile.url,
                    updaterFile.sha256
                ),
                changelog = version.changelog
            )
        }

        verbose { "Result version map is $versionMap" }

        return versionMap
    }

    @Throws(SubGhzProvisioningException::class)
    override suspend fun getSubGhzProvisioning(): SubGhzProvisioningModel {
        val response = client.get(
            urlString = SUB_GHZ_URL
        ).body<SubGhzProvisioningResponse>()

        verbose { "Receive subghz info $response" }

        if (response.error != null) {
            throw SubGhzProvisioningException(response.error.code, response.error.text)
        }
        val successfulResponse =
            response.success ?: throw SubGhzProvisioningException(
                errorCode = -1,
                "Not found response"
            )

        return SubGhzProvisioningModel(
            countries = successfulResponse
                .countriesBands
                .mapKeys { it.key.uppercase() }
                .mapValues { entry ->
                    entry.value.mapNotNull {
                        successfulResponse.bands[it]?.toSubGhzProvisioningBand()
                    }
                },
            country = successfulResponse.countryCode?.uppercase(),
            defaults = successfulResponse
                .defaultBands
                .mapNotNull { successfulResponse.bands[it]?.toSubGhzProvisioningBand() }
        )
    }

    override fun download(
        distributionFile: DistributionFile,
        target: File,
        decompress: Boolean
    ): Flow<DownloadProgress> = channelFlow {
        info { "Request download $distributionFile" }
        if (decompress) {
            storageProvider.useTemporaryFile { tempFile ->
                downloadAndUnpackDelegateApi.download(
                    distributionFile,
                    tempFile.toFile()
                ) { processedBytes, totalBytes ->
                    send(DownloadProgress.InProgress(processedBytes, totalBytes))
                }
                info { "File downloaded in $tempFile" }

                downloadAndUnpackDelegateApi.unpack(tempFile.toFile(), target)
                info {
                    "Unpack finished in ${target.absolutePath} ${target.listFiles()?.size} files"
                }
            }
        } else {
            downloadAndUnpackDelegateApi.download(
                distributionFile,
                target
            ) { processedBytes, totalBytes ->
                send(DownloadProgress.InProgress(processedBytes, totalBytes))
            }
            info { "File downloaded in ${target.absolutePath}" }
        }
    }
}

private fun String.clearVersion(): String {
    return replace("-rc", "").trim()
}
