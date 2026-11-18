package dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.decompose.internal

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.remotecontrols.api.CategoriesScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.api.PauseSyncDialogDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.composable.DeviceCategoriesScreen
import dev.namelessnanashi.flipperzero.remotecontrols.impl.categories.presentation.decompose.DeviceCategoriesComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, CategoriesScreenDecomposeComponent.Factory::class)
class CategoriesScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted onBackClick: () -> Unit,
    @Assisted onCategoryClick: (Long, String) -> Unit,
    deviceCategoriesComponentFactory: DeviceCategoriesComponent.Factory,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    private val pauseSyncDialogComponentFactory: PauseSyncDialogDecomposeComponent.Factory
) : CategoriesScreenDecomposeComponent(componentContext) {
    private val deviceCategoriesComponent = deviceCategoriesComponentFactory.invoke(
        componentContext = childContext("DeviceCategoriesComponent"),
        onBackClick = onBackClick,
        onCategoryClick = onCategoryClick
    )
    private val pauseSyncDialogComponent = pauseSyncDialogComponentFactory.invoke(
        componentContext = childContext("PauseSyncDialogDecomposeComponent_CategoriesScreenDecomposeComponent"),
        onProceed = {},
        onBack = onBackClick
    )

    @Composable
    override fun Render() {
        DeviceCategoriesScreen(
            deviceCategoriesComponent = deviceCategoriesComponent,
            errorsRenderer = errorsRenderer
        )
        pauseSyncDialogComponent.Render()
    }
}
