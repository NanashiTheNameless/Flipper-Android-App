package dev.namelessnanashi.flipperzero.remotecontrols.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialog
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.multichoice.FlipperMultiChoiceDialogModel
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.remotecontrols.api.PauseSyncDialogDecomposeComponent
import dev.namelessnanashi.flipperzero.remotecontrols.main.impl.R
import dev.namelessnanashi.flipperzero.remotecontrols.impl.viewmodel.PauseSyncViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@Suppress("LongParameterList")
@ContributesAssistedFactory(AppGraph::class, PauseSyncDialogDecomposeComponent.Factory::class)
class PauseSyncDialogDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onProceed: () -> Unit,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val pauseSyncViewModelProvider: Provider<PauseSyncViewModel>
) : PauseSyncDialogDecomposeComponent(componentContext) {
    @Composable
    override fun Render() {
        val pauseSyncViewModel = viewModelWithFactory(null) {
            pauseSyncViewModelProvider.invoke()
        }
        val isDialogVisible by pauseSyncViewModel.isDialogVisible.collectAsState()

        val dialogModel = remember(onBack, onProceed, pauseSyncViewModel) {
            FlipperMultiChoiceDialogModel.Builder()
                .setTitle(composableText = { stringResource(R.string.rcm_pause_sync_dialog_title) })
                .setDescription(composableText = { stringResource(R.string.rcm_pause_sync_dialog_desc) })
                .setOnDismissRequest(onBack::invoke)
                .addButton(
                    textComposable = { stringResource(R.string.rcm_pause_sync_dialog_proceed) },
                    onClick = {
                        pauseSyncViewModel.stop()
                        onProceed.invoke()
                    },
                    isActive = true
                )
                .addButton(
                    textComposable = { stringResource(R.string.rcm_pause_sync_dialog_cancel) },
                    onClick = {
                        pauseSyncViewModel.dismiss()
                        onBack.invoke()
                    }
                )
                .build()
        }
        if (isDialogVisible) {
            FlipperMultiChoiceDialog(model = dialogModel)
        }
    }
}
