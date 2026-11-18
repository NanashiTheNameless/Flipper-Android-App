package dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api

import dev.namelessnanashi.flipperzero.bridge.connection.feature.common.api.FDeviceFeatureApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDeleteApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileDownloadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileStorageMD5Api
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileTimestampApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileUploadApi
import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FListingStorageApi

interface FStorageFeatureApi : FDeviceFeatureApi {
    fun md5Api(): FFileStorageMD5Api
    fun listingApi(): FListingStorageApi
    fun downloadApi(): FFileDownloadApi
    fun uploadApi(): FFileUploadApi
    fun deleteApi(): FFileDeleteApi
    fun timestampApi(): FFileTimestampApi
}
