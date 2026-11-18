package dev.namelessnanashi.flipperzero.archive.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class CategoryDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            categoryType: CategoryType,
            onBack: DecomposeOnBackParameter
        ): CategoryDecomposeComponent<*>
    }
}
