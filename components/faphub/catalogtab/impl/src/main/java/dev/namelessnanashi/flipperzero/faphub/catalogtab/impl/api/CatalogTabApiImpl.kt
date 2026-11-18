package dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.faphub.catalogtab.api.CatalogTabApi
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.composable.ComposableCatalogTabScreen
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel.CategoriesViewModel
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel.FapsListViewModel
import dev.namelessnanashi.flipperzero.faphub.catalogtab.impl.viewmodel.InstallAllViewModel
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort
import dev.namelessnanashi.flipperzero.faphub.errors.api.FapHubComposableErrorsRenderer
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapInstallationUIApi
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.toFapButtonConfig
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<CatalogTabApi>())
class CatalogTabApiImpl @Inject constructor(
    private val fapInstallationUIApi: FapInstallationUIApi,
    private val errorsRenderer: FapHubComposableErrorsRenderer,
    private val categoriesViewModelProvider: Provider<CategoriesViewModel>,
    private val fapsListViewModelProvider: Provider<FapsListViewModel>,
    private val installAllViewModelProvider: Provider<InstallAllViewModel>
) : CatalogTabApi {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun ComposableCatalogTab(
        componentContext: ComponentContext,
        onOpenFapItem: (FapItemShort) -> Unit,
        onCategoryClick: (FapCategory) -> Unit
    ) {
        ComposableCatalogTabScreen(
            onOpenFapItem = onOpenFapItem,
            onCategoryClick = onCategoryClick,
            errorsRenderer = errorsRenderer,
            installationButton = { fapItem, modifier ->
                fapInstallationUIApi.ComposableButton(
                    config = fapItem?.toFapButtonConfig(),
                    modifier = modifier,
                    fapButtonSize = FapButtonSize.COMPACTED,
                    componentContext = componentContext
                )
            },
            categoriesViewModel = componentContext.viewModelWithFactory(key = null) {
                categoriesViewModelProvider.invoke()
            },
            fapsListViewModel = componentContext.viewModelWithFactory(key = null) {
                fapsListViewModelProvider.invoke()
            },
            installAllViewModel = componentContext.viewModelWithFactory(key = null) {
                installAllViewModelProvider.invoke()
            }
        )
    }
}
