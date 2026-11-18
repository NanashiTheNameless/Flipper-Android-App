package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.model

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import kotlinx.collections.immutable.ImmutableList

sealed class CategoriesLoadState {
    object Loading : CategoriesLoadState()

    data class Loaded(val categories: ImmutableList<FapCategory>) : CategoriesLoadState()

    data class Error(val throwable: Throwable) : CategoriesLoadState()
}
