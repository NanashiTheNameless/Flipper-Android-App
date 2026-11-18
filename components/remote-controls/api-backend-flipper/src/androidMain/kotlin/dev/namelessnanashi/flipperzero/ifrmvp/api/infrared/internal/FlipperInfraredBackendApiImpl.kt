package dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.internal

import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.api.FStorageInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storageinfo.model.flashSdStats
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.FDeviceOrchestrator
import dev.namelessnanashi.flipperzero.bridge.connection.orchestrator.api.model.FDeviceConnectStatus
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FirmwareNotSupported
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FlipperNotConnected
import dev.namelessnanashi.flipperzero.faphub.installation.manifest.error.NoSdCardException
import dev.namelessnanashi.flipperzero.faphub.target.api.FlipperTargetProviderApi
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.FlipperInfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.InfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandsResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.CategoriesResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.IfrFileContentResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.InfraredsResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.PagesLayoutBackendModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalRequestModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalResponseModel
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FlipperInfraredBackendApi>())
class FlipperInfraredBackendApiImpl @Inject constructor(
    private val api: InfraredBackendApi,
    private val flipperTargetProviderApi: FlipperTargetProviderApi,
    private val fFeatureProvider: FFeatureProvider,
    private val fDeviceOrchestrator: FDeviceOrchestrator
) : FlipperInfraredBackendApi {
    private suspend fun isSdCardPresent(): Boolean {
        return fFeatureProvider.get<FStorageInfoFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FStorageInfoFeatureApi> }
            .map { status -> status?.featureApi }
            .flatMapLatest { feature -> feature?.getStorageInformationFlow() ?: flowOf(null) }
            .first()
            ?.flashSdStats != null
    }

    private suspend fun isDeviceConnected(): Boolean {
        return fDeviceOrchestrator.getState().first() is FDeviceConnectStatus.Connected
    }

    @Suppress("ThrowsCount", "RethrowCaughtException")
    private suspend fun <T> wrapRequest(block: suspend () -> T): T {
        return try {
            when (flipperTargetProviderApi.getFlipperTarget().value) {
                FlipperTarget.NotConnected -> throw FlipperNotConnected()
                FlipperTarget.Unsupported -> throw FirmwareNotSupported()
                else -> Unit
            }
            if (!isDeviceConnected()) {
                throw FlipperNotConnected()
            }
            if (!isSdCardPresent()) {
                throw NoSdCardException()
            }
            block.invoke()
        } catch (e: Throwable) {
            throw e
        }
    }

    override suspend fun getCategories(): CategoriesResponse {
        return wrapRequest { api.getCategories() }
    }

    override suspend fun getManufacturers(categoryId: Long): BrandsResponse {
        return wrapRequest { api.getManufacturers(categoryId) }
    }

    override suspend fun getSignal(request: SignalRequestModel): SignalResponseModel {
        return wrapRequest { api.getSignal(request) }
    }

    override suspend fun getIfrFileContent(ifrFileId: Long): IfrFileContentResponse {
        return wrapRequest { api.getIfrFileContent(ifrFileId) }
    }

    override suspend fun getUiFile(ifrFileId: Long): PagesLayoutBackendModel {
        return wrapRequest { api.getUiFile(ifrFileId) }
    }

    override suspend fun getInfrareds(brandId: Long): InfraredsResponse {
        return wrapRequest { api.getInfrareds(brandId) }
    }
}
