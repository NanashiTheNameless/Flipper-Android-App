package dev.namelessnanashi.flipperzero.faphub.installation.button.impl.composable.elements

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model.FAP_DOWNLOAD_PROGRESS_SHARE
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.namelessnanashi.flipperzero.core.ktx.jre.roundPercentToString
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.installation.button.api.FapButtonSize
import dev.namelessnanashi.flipperzero.faphub.installation.button.impl.R

@Composable
fun ComposableInProgressFapButton(
    percent: Float,
    fapButtonSize: FapButtonSize,
    color: Color,
    @StringRes installLabelCompact: Int,
    @StringRes installLabelLarge: Int,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
    fapButtonModifier: Modifier = Modifier,
    backgroundDownload: Boolean = false
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        when (fapButtonSize) {
            FapButtonSize.COMPACTED -> {}
            FapButtonSize.LARGE -> ComposableInProgressFapCancel(
                Modifier
                    .padding(end = 12.dp)
                    .size(46.dp)
                    .clickableRipple(onClick = onCancel)
            )
        }
        ComposableInProgressFapButtonInternal(
            percent = percent,
            backgroundDownload = backgroundDownload,
            fapButtonSize = fapButtonSize,
            color = color,
            installLabelCompact = installLabelCompact,
            installLabelLarge = installLabelLarge,
            modifier = fapButtonModifier
        )
        when (fapButtonSize) {
            FapButtonSize.COMPACTED -> ComposableInProgressFapCancel(
                Modifier
                    .padding(start = 12.dp)
                    .size(34.dp)
                    .clickableRipple(onClick = onCancel)
            )

            FapButtonSize.LARGE -> {}
        }
    }
}

@Composable
private fun ComposableInProgressFapCancel(
    modifier: Modifier = Modifier
) {
    Icon(
        modifier = modifier,
        painter = painterResource(R.drawable.ic_cancel),
        contentDescription = stringResource(R.string.faphub_installation_cancel_desc),
        tint = LocalPallet.current.text40
    )
}

@Composable
private fun ComposableInProgressFapButtonInternal(
    percent: Float,
    fapButtonSize: FapButtonSize,
    color: Color,
    @StringRes installLabelCompact: Int,
    @StringRes installLabelLarge: Int,
    backgroundDownload: Boolean,
    modifier: Modifier = Modifier
) {
    // Fork: the download from the catalog and the upload to the Flipper are shown as two
    // steps, each with its own 0-100 %
    val isDownloading = backgroundDownload || percent < FAP_DOWNLOAD_PROGRESS_SHARE
    val stepPercent = if (isDownloading) {
        percent / FAP_DOWNLOAD_PROGRESS_SHARE
    } else {
        (percent - FAP_DOWNLOAD_PROGRESS_SHARE) / (1f - FAP_DOWNLOAD_PROGRESS_SHARE)
    }.coerceIn(0f, 1f)
    val label = stringResource(
        when (fapButtonSize) {
            FapButtonSize.COMPACTED -> if (isDownloading) {
                R.string.faphub_installation_step_download_compact
            } else {
                installLabelCompact
            }
            FapButtonSize.LARGE -> if (isDownloading) {
                R.string.faphub_installation_step_download
            } else {
                installLabelLarge
            }
        }
    )
    val buttonModifier = when (fapButtonSize) {
        FapButtonSize.COMPACTED -> modifier.width(92.dp)
        FapButtonSize.LARGE -> modifier
    }
    Box(
        modifier = buttonModifier
            .height(fapButtonSize.heightDp.dp)
            .clip(RoundedCornerShape(6.dp))
            .background(LocalPallet.current.background)
            .border(2.dp, color, RoundedCornerShape(6.dp)),
        contentAlignment = Alignment.Center
    ) {
        Text(
            modifier = Modifier.padding(vertical = 4.dp),
            text = "$label ${stepPercent.roundPercentToString()}",
            textAlign = TextAlign.Center,
            style = LocalTypography.current.fapHubButtonProgressText.copy(
                fontSize = fapButtonSize.textSizeSp.sp
            ),
            maxLines = 1,
            color = color
        )
    }
}
