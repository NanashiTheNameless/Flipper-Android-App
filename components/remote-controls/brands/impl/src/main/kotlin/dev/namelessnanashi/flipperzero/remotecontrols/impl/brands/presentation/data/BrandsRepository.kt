package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.data

import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandModel

interface BrandsRepository {
    suspend fun fetchBrands(categoryId: Long): Result<List<BrandModel>>
}
