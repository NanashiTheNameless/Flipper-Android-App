package dev.namelessnanashi.flipperzero.archive.category.api

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.archive.api.CategoryDecomposeComponent
import dev.namelessnanashi.flipperzero.archive.category.model.CategoryNavigationConfig
import dev.namelessnanashi.flipperzero.archive.model.CategoryType
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.popOr
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, CategoryDecomposeComponent.Factory::class)
class CategoryDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted categoryType: CategoryType,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val categoryFactory: CategoryScreenDecomposeComponentImpl.Factory
) : CategoryDecomposeComponent<CategoryNavigationConfig>(), ComponentContext by componentContext {
    override val stack: Value<ChildStack<CategoryNavigationConfig, DecomposeComponent>> =
        childStack(
            source = navigation,
            serializer = CategoryNavigationConfig.serializer(),
            initialStack = {
                listOf(CategoryNavigationConfig.Category(categoryType))
            },
            handleBackButton = true,
            childFactory = ::child,
        )

    private fun child(
        config: CategoryNavigationConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is CategoryNavigationConfig.Category -> categoryFactory(
            componentContext = componentContext,
            categoryType = config.categoryType,
            navigation = navigation,
            onBack = { navigation.popOr(onBack::invoke) }
        )
    }
}
