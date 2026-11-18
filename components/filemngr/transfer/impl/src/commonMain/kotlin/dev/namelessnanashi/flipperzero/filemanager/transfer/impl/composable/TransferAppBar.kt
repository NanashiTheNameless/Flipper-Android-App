package dev.namelessnanashi.flipperzero.filemanager.transfer.impl.composable

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.filemanager.transfer.api.model.TransferType
import dev.namelessnanashi.flipperzero.filemanager.transfer.impl.viewmodel.OptionsViewModel
import flipperapp.components.filemngr.transfer.impl.generated.resources.fmt_appbar_title_moving
import org.jetbrains.compose.resources.stringResource
import flipperapp.components.filemngr.transfer.impl.generated.resources.Res as FMT

@Composable
fun TransferAppBar(
    transferType: TransferType,
    onBack: () -> Unit,
    optionsState: OptionsViewModel.State,
    onOptionsAction: (OptionsViewModel.Action) -> Unit,
    onCreateFolder: () -> Unit
) {
    OrangeAppBar(
        title = when (transferType) {
            TransferType.MOVE -> stringResource(FMT.string.fmt_appbar_title_moving)
        },
        onBack = onBack::invoke,
        endBlock = {
            MoreIconComposable(
                optionsState = optionsState,
                onAction = onOptionsAction,
                onCreateFolderClick = onCreateFolder,
            )
        }
    )
}
