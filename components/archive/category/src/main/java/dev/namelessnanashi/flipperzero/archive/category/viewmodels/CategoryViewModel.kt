package dev.namelessnanashi.flipperzero.archive.category.viewmodels

import dev.namelessnanashi.flipperzero.archive.category.model.CategoryState
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.DeleteKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.collections.immutable.toImmutableList
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch

class CategoryViewModel @AssistedInject constructor(
    @Assisted categoryType: CategoryType,
    private val parser: KeyParser,
    private val simpleKeyApi: SimpleKeyApi,
    private val deleteKeyApi: DeleteKeyApi,
    private val synchronizationState: SynchronizationApi
) : DecomposeViewModel() {
    private val categoryState = MutableStateFlow<CategoryState>(CategoryState.Loading)

    init {
        viewModelScope.launch {
            when (categoryType) {
                is CategoryType.ByFileType -> simpleKeyApi.getExistKeysAsFlow(categoryType.fileType)
                CategoryType.Deleted -> deleteKeyApi.getDeletedKeyAsFlow()
            }.map { list ->
                list.map {
                    parser.parseKey(it) to it
                }
            }.collect {
                categoryState.emit(CategoryState.Loaded(it.toImmutableList()))
            }
        }
    }

    fun getState(): StateFlow<CategoryState> = categoryState

    fun getSynchronizationState() = synchronizationState.getSynchronizationState()

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            categoryType: CategoryType
        ): CategoryViewModel
    }
}
