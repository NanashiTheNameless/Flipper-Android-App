package dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.data

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.FlipperInfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.DeviceCategory
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<DeviceCategoriesRepository>())
class BackendDeviceCategoriesRepository @Inject constructor(
    private val infraredBackendApi: FlipperInfraredBackendApi,
) : DeviceCategoriesRepository {

    override suspend fun fetchCategories(): Result<List<DeviceCategory>> = runCatching {
        withContext(FlipperDispatchers.workStealingDispatcher) {
            infraredBackendApi.getCategories().categories
        }
    }
}
