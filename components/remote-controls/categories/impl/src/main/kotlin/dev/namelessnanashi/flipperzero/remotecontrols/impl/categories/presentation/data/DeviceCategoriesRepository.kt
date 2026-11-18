package dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.data

import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.DeviceCategory

interface DeviceCategoriesRepository {
    suspend fun fetchCategories(): Result<List<DeviceCategory>>
}
