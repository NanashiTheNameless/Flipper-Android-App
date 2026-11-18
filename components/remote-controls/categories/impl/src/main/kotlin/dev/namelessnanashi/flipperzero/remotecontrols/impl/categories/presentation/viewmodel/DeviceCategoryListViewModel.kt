package dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.toFapHubError
import dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.data.DeviceCategoriesRepository
import dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.decompose.DeviceCategoriesComponent
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import dev.zacsweers.metro.Inject

class DeviceCategoryListViewModel @Inject constructor(
    private val deviceCategoriesRepository: DeviceCategoriesRepository
) : DecomposeViewModel(), LogTagProvider {
    override val TAG = "DeviceCategoryListViewModel"
    private val _model = MutableStateFlow<DeviceCategoriesComponent.Model>(
        value = DeviceCategoriesComponent.Model.Loading
    )
    val model = _model.asStateFlow()

    fun tryLoad() = viewModelScope.launch {
        _model.emit(DeviceCategoriesComponent.Model.Loading)
        deviceCategoriesRepository.fetchCategories()
            .onFailure {
                _model.emit(DeviceCategoriesComponent.Model.Error(it.toFapHubError()))
            }
            .onFailure { throwable -> error(throwable) { "#tryLoad could not fetch categories" } }
            .onSuccess { categories ->
                _model.emit(DeviceCategoriesComponent.Model.Loaded(categories.toImmutableList()))
            }
    }

    init {
        tryLoad()
    }
}
