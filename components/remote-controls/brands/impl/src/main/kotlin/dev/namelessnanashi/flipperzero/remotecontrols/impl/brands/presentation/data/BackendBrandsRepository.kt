package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.data

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.ifrmvp.api.infrared.FlipperInfraredBackendApi
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandModel
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.withContext
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<BrandsRepository>())
class BackendBrandsRepository @Inject constructor(
    private val infraredBackendApi: FlipperInfraredBackendApi,
) : BrandsRepository {
    override suspend fun fetchBrands(
        categoryId: Long
    ): Result<List<BrandModel>> = withContext(FlipperDispatchers.workStealingDispatcher) {
        runCatching { infraredBackendApi.getManufacturers(categoryId).brands }
    }
}
