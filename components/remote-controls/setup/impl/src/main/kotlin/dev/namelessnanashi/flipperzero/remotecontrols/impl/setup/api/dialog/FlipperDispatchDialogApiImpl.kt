package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.api.dialog

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.remotecontrols.api.FlipperDispatchDialogApi
import dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.api.dialog.composable.SetupFlipperDialogComposable
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedInject
import uk.kulikov.metro.assisted.ContributesAssistedFactory

@ContributesAssistedFactory(AppGraph::class, FlipperDispatchDialogApi.Factory::class)
class FlipperDispatchDialogApiImpl @AssistedInject constructor(
    @Assisted private val onBack: DecomposeOnBackParameter
) : FlipperDispatchDialogApi {
    @Composable
    override fun Render(
        dialogType: FlipperDispatchDialogApi.DialogType?,
        onDismiss: () -> Unit
    ) {
        val deeplinkHandler = LocalDeeplinkHandler.current
        SetupFlipperDialogComposable(
            flipperDialog = dialogType,
            onDismiss = onDismiss,
            openDeviceTab = {
                deeplinkHandler.handleDeeplink(
                    Deeplink.BottomBar.OpenTab(
                        DeeplinkBottomBarTab.DEVICE
                    )
                )
                onBack.invoke()
            }
        )
    }
}
