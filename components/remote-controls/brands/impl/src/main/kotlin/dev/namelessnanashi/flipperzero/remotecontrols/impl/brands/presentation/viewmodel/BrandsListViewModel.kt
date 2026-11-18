package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.viewmodel

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.FapHubError
import dev.namelessnanashi.flipperzero.faphub.errors.api.throwable.toFapHubError
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.BrandModel
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.data.BrandsRepository
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.ImmutableList
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

class BrandsListViewModel @AssistedInject constructor(
    private val brandsRepository: BrandsRepository,
    @Assisted private val categoryId: Long
) : DecomposeViewModel(), LogTagProvider {
    override val TAG: String = "BrandsListViewModel"
    private val _state = MutableStateFlow<State>(State.Loading)
    val state = _state.asStateFlow()

    fun tryLoad() = viewModelScope.launch {
        _state.update { State.Loading }
        brandsRepository.fetchBrands(categoryId)
            .onSuccess { _state.emit(State.Loaded(it.toImmutableList())) }
            .onFailure {
                _state.emit(State.Error(it.toFapHubError()))
            }
            .onFailure { throwable -> error(throwable) { "#tryLoad could not load brands" } }
    }

    init {
        tryLoad()
    }

    sealed interface State {
        data object Loading : State
        data class Loaded(val brands: ImmutableList<BrandModel>) : State
        data class Error(val throwable: FapHubError) : State
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            categoryId: Long
        ): BrandsListViewModel
    }
}
