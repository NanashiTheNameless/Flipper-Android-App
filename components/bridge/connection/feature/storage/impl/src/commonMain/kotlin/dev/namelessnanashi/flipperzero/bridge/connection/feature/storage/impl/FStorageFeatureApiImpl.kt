package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.impl

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.FStorageFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDeleteApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDownloadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileStorageMD5Api
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileTimestampApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi

class FStorageFeatureApiImpl(
    private val md5Api: FFileStorageMD5Api,
    private val fListingStorageApi: FListingStorageApi,
    private val fileUploadApi: FFileUploadApi,
    private val fileDownloadApi: FFileDownloadApi,
    private val deleteApi: FFileDeleteApi,
    private val timestampApi: FFileTimestampApi
) : FStorageFeatureApi {
    override fun md5Api() = md5Api
    override fun listingApi() = fListingStorageApi
    override fun uploadApi() = fileUploadApi
    override fun downloadApi() = fileDownloadApi
    override fun deleteApi() = deleteApi
    override fun timestampApi() = timestampApi
}
