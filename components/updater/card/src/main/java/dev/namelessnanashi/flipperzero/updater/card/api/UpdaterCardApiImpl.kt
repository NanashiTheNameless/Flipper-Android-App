package dev.namelessnanashi.flipperzero.updater.card.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Modifier
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.updater.api.UpdaterCardApi
import dev.namelessnanashi.flipperzero.updater.card.composable.ComposableUpdaterCardInternal
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateCardViewModel
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateRequestViewModel
import dev.namelessnanashi.flipperzero.updater.card.viewmodel.UpdateStateViewModel
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

@ContributesBinding(AppGraph::class)
class UpdaterCardApiImpl @Inject constructor(
    private val updateCardFactory: UpdateCardViewModel.Factory,
    private val updateStateViewModelProvider: Provider<UpdateStateViewModel>,
    private val updateRequestViewModelProvider: Provider<UpdateRequestViewModel>
) : UpdaterCardApi {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun ComposableUpdaterCard(
        modifier: Modifier,
        componentContext: ComponentContext,
        deeplink: Deeplink.BottomBar.DeviceTab.WebUpdate?,
        onStartUpdateRequest: (UpdateRequest) -> Unit,
        requestRefresh: Boolean,
        onRefreshRequestExecute: () -> Unit
    ) {
        val updateStateViewModel: UpdateStateViewModel =
            componentContext.viewModelWithFactory(key = null) {
                updateStateViewModelProvider.invoke()
            }
        val updateCardViewModel: UpdateCardViewModel = componentContext.viewModelWithFactory(
            key = deeplink?.toString()
        ) {
            updateCardFactory(deeplink)
        }
        val updateRequestViewModel: UpdateRequestViewModel =
            componentContext.viewModelWithFactory(key = null) {
                updateRequestViewModelProvider.invoke()
            }

        LaunchedEffect(requestRefresh, onRefreshRequestExecute) {
            if (requestRefresh) {
                updateCardViewModel.refresh()
                onRefreshRequestExecute()
            }
        }

        ComposableUpdaterCardInternal(
            modifier = modifier,
            onStartUpdateRequest = onStartUpdateRequest,
            updateStateViewModel = updateStateViewModel,
            updateCardViewModel = updateCardViewModel,
            updateRequestViewModel = updateRequestViewModel
        )
    }
}
