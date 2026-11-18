package dev.namelessnanashi.flipperzero.faphub.category.impl.model

import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import kotlinx.serialization.Serializable

@Serializable
sealed class FapCategoryNavigationConfig {
    @Serializable
    data class CategoryList(val fapCategory: FapCategory) : FapCategoryNavigationConfig()

    @Serializable
    data class FapScreen(val id: String) : FapCategoryNavigationConfig()

    @Serializable
    data object Search : FapCategoryNavigationConfig()
}
