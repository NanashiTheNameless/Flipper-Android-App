package dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.decompose.internal

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.remotecontrols.api.InfraredsScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.composable.InfraredsScreen
import dev.namelessnanashi.flipperzero.remotecontrols.impl.brands.presentation.viewmodel.InfraredsListViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, InfraredsScreenDecomposeComponent.Factory::class)
class InfraredFilesDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val brandId: Long,
    @Assisted private val onBackClick: () -> Unit,
    @Assisted private val onRemoteFound: (Long, String) -> Unit,
    private val infraredsListViewModelFactory: InfraredsListViewModel.Factory,
) : InfraredsScreenDecomposeComponent(componentContext) {

    @Composable
    override fun Render() {
        val viewModel = viewModelWithFactory(null) {
            infraredsListViewModelFactory.invoke(brandId)
        }
        InfraredsScreen(
            viewModel = viewModel,
            onBack = onBackClick,
            onReload = viewModel::tryLoad,
            onClick = {
                onRemoteFound.invoke(it.id, it.folderName)
            },
        )
    }
}
