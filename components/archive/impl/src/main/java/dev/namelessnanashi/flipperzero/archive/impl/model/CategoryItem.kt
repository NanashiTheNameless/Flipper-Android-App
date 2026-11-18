package dev.namelessnanashi.flipperzero.archive.impl.model

import androidx.annotation.DrawableRes
import dev.namelessnanashi.flipperzero.archive.model.CategoryType

data class CategoryItem(
    @DrawableRes val iconId: Int?,
    val title: String,
    val count: Int?,
    val categoryType: CategoryType
)
