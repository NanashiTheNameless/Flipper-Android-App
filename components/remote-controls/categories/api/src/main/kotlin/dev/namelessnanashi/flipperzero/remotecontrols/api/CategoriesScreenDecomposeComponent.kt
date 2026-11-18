package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class CategoriesScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onBackClick: () -> Unit,
            onCategoryClick: (categoryId: Long, categoryName: String) -> Unit
        ): CategoriesScreenDecomposeComponent
    }
}
