package dev.namelessnanashi.flipperzero.ifrmvp.api.infrared

import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandsResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.CategoriesResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.IfrFileContentResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.InfraredsResponse
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.PagesLayoutBackendModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalRequestModel
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalResponseModel

interface InfraredBackendApi {
    suspend fun getCategories(): CategoriesResponse

    suspend fun getManufacturers(categoryId: Long): BrandsResponse

    suspend fun getSignal(request: SignalRequestModel): SignalResponseModel

    suspend fun getIfrFileContent(ifrFileId: Long): IfrFileContentResponse

    suspend fun getUiFile(ifrFileId: Long): PagesLayoutBackendModel

    suspend fun getInfrareds(brandId: Long): InfraredsResponse
}
