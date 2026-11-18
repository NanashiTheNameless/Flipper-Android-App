package dev.namelessnanashi.flipperzero.updater.impl.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.getinfo.api.FGattInfoFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureProvider
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.FFeatureStatus
import dev.namelessnanashi.flipperzero.bridge.connection.feature.provider.api.get
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.updater.api.FirmwareVersionBuilderApi
import dev.namelessnanashi.flipperzero.updater.api.FlipperVersionProviderApi
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<FlipperVersionProviderApi>())
class FlipperVersionProviderApiImpl @Inject constructor(
    private val firmwareVersionBuilderApi: FirmwareVersionBuilderApi,
    private val fFeatureProvider: FFeatureProvider
) : FlipperVersionProviderApi, LogTagProvider {
    override val TAG: String = "FlipperVersionProviderApiImpl"

    override fun getCurrentFlipperVersion(): Flow<FirmwareVersion?> {
        return fFeatureProvider.get<FGattInfoFeatureApi>()
            .map { status -> status as? FFeatureStatus.Supported<FGattInfoFeatureApi> }
            .flatMapLatest { status -> status?.featureApi?.getGattInfoFlow() ?: flowOf(null) }
            .map { gattInfo -> gattInfo?.softwareVersion }
            .map { softwareVersion ->
                if (softwareVersion != null) {
                    firmwareVersionBuilderApi.buildFirmwareVersionFromString(softwareVersion)
                } else {
                    null
                }
            }
    }
}
