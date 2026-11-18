package dev.namelessnanashi.flipperzero.filemanager.download.impl.composable

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import dev.namelessnanashi.flipperzero.core.ktx.jre.toFormattedSize
import dev.namelessnanashi.flipperzero.filemanager.download.impl.viewmodel.DownloadViewModel
import dev.namelessnanashi.flipperzero.filemanager.ui.components.transfer.FileTransferFullScreenComposable
import flipperapp.components.filemngr.download.impl.generated.resources.fm_cancel
import flipperapp.components.filemngr.download.impl.generated.resources.fm_downloading
import flipperapp.components.filemngr.download.impl.generated.resources.fm_in_progress_file_size
import flipperapp.components.filemngr.download.impl.generated.resources.fm_in_progress_speed
import org.jetbrains.compose.resources.stringResource
import flipperapp.components.filemngr.download.impl.generated.resources.Res as FDR

@Composable
fun DownloadingComposable(
    state: DownloadViewModel.State.Downloading,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier
) {
    FileTransferFullScreenComposable(
        modifier = modifier,
        title = stringResource(FDR.string.fm_downloading),
        actionText = stringResource(FDR.string.fm_cancel),
        onActionClick = onCancel,
        progressTitle = state.fullPath.name,
        progress = if (state.totalSize == 0L) 0f else state.downloadedSize / state.totalSize.toFloat(),
        progressText = stringResource(
            FDR.string.fm_in_progress_file_size,
            state.downloadedSize.toFormattedSize(),
            state.totalSize.toFormattedSize()
        ),
        speedText = when (state.downloadSpeed) {
            0L -> null
            else -> stringResource(
                FDR.string.fm_in_progress_speed,
                state.downloadSpeed.toFormattedSize(),
            )
        }
    )
}
