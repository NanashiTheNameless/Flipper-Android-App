package dev.namelessnanashi.flipperzero.filemanager.ui.components.transfer

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableCancelButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography

@Composable
fun FileTransferFullScreenComposable(
    title: String,
    actionText: String,
    onActionClick: () -> Unit,
    progressTitle: String,
    progress: Float,
    modifier: Modifier = Modifier,
    progressText: String? = null,
    progressDetailText: String? = null,
    speedText: String? = null
) {
    Box(
        modifier = modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Column(
            verticalArrangement = Arrangement.SpaceBetween,
            horizontalAlignment = Alignment.CenterHorizontally,
            modifier = Modifier.fillMaxSize()
        ) {
            Text(
                text = title,
                style = LocalTypography.current.titleB18,
                color = LocalPalletV2.current.text.title.primary,
                modifier = Modifier.fillMaxWidth(),
                textAlign = TextAlign.Center
            )

            ComposableCancelButton(
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 12.dp),
                text = actionText,
                onClick = onActionClick
            )
        }
        FileTransferProgressComposable(
            progressTitle = progressTitle,
            progressDetailText = progressDetailText,
            progress = progress,
            progressText = progressText,
            speedText = speedText
        )
    }
}
