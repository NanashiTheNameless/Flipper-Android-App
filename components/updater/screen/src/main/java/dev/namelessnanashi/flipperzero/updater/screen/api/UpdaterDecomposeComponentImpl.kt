package dev.namelessnanashi.flipperzero.updater.screen.api

import android.app.Activity
import android.view.WindowManager
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.changelog.api.ChangelogFormatterApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.updater.api.UpdaterDecomposeComponent
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.screen.composable.ComposableCancelDialog
import dev.namelessnanashi.flipperzero.updater.screen.composable.ComposableUpdaterScreen
import dev.namelessnanashi.flipperzero.updater.screen.model.UpdaterScreenState
import dev.namelessnanashi.flipperzero.updater.screen.viewmodel.FlipperColorViewModel
import dev.namelessnanashi.flipperzero.updater.screen.viewmodel.UpdaterViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory
import dev.zacsweers.metro.Provider

@ContributesAssistedFactory(AppGraph::class, UpdaterDecomposeComponent.Factory::class)
class UpdaterDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val updateRequest: UpdateRequest?,
    @Assisted private val onBack: DecomposeOnBackParameter,
    private val flipperColorViewModelProvider: Provider<FlipperColorViewModel>,
    private val updaterViewModelProvider: Provider<UpdaterViewModel>,
    private val changelogFormatterApi: ChangelogFormatterApi
) : UpdaterDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val flipperColorViewModel = viewModelWithFactory(key = null) {
            flipperColorViewModelProvider.invoke()
        }
        val updaterViewModel = viewModelWithFactory(key = null) {
            updaterViewModelProvider.invoke()
        }

        LaunchedEffect(key1 = Unit) {
            updaterViewModel.start(updateRequest)
        }

        val context = LocalContext.current
        DisposableEffect(Unit) {
            val window = (context as? Activity)?.window
            window?.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            onDispose {
                window?.clearFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)
            }
        }

        val flipperColor by flipperColorViewModel.getFlipperColor().collectAsState()

        val updaterScreenState by updaterViewModel.getState().collectAsState()
        if (updaterScreenState is UpdaterScreenState.Finish) {
            LaunchedEffect(key1 = Unit) {
                onBack()
            }
        }

        val onAbortUpdate = updaterViewModel::cancelUpdate
        var isCancelDialogOpen by remember { mutableStateOf(false) }
        val changelog = updateRequest?.changelog
        ComposableUpdaterScreen(
            updaterScreenState = updaterScreenState,
            flipperColor = flipperColor,
            onCancel = { isCancelDialogOpen = true },
            onRetry = { updaterViewModel.retry(updateRequest) },
            changelog = remember(changelog) {
                if (changelog != null) {
                    changelogFormatterApi.format(changelog)
                } else {
                    null
                }
            }
        )
        if (isCancelDialogOpen) {
            when (updaterScreenState) {
                is UpdaterScreenState.Failed -> onAbortUpdate()
                else ->
                    ComposableCancelDialog(
                        onAbort = {
                            isCancelDialogOpen = false
                            updaterViewModel.cancelUpdate()
                        },
                        onContinue = { isCancelDialogOpen = false }
                    )
            }
        }
    }
}
