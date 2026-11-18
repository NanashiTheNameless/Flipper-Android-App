package dev.namelessnanashi.flipperzero.archive.category.model

import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import kotlinx.serialization.Serializable

@Serializable
sealed class CategoryNavigationConfig {
    @Serializable
    data class Category(val categoryType: CategoryType) : CategoryNavigationConfig()
}
