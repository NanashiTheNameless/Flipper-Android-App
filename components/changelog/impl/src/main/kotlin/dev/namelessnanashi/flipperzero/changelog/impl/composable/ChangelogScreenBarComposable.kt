package dev.namelessnanashi.flipperzero.changelog.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.changelog.impl.R
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPalletV2
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.info.shared.getColorByChannel
import dev.namelessnanashi.flipperzero.info.shared.getTextByVersion
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarCancelIcon
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableKeyScreenAppBar
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion

@Composable
fun ChangelogScreenBarComposable(
    firmwareVersion: FirmwareVersion,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    ComposableKeyScreenAppBar(
        modifier = modifier,
        centerBlock = {
            Column(
                modifier = it,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text(
                    text = stringResource(R.string.whatsnew_title),
                    color = LocalPalletV2.current.text.title.primary,
                    style = LocalTypography.current.titleEB18
                )
                Text(
                    text = getTextByVersion(firmwareVersion),
                    color = getColorByChannel(firmwareVersion.channel),
                    style = LocalTypography.current.subtitleM12
                )
            }
        },
        endBlock = {
            ComposableBarCancelIcon(
                modifier = it,
                onClick = onBack,
            )
        }
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ChangelogScreenBarComposablePreview() {
    FlipperThemeInternal {
        ChangelogScreenBarComposable(
            firmwareVersion = FirmwareVersion(
                channel = FirmwareChannel.DEV,
                version = "0.99"
            ),
            onBack = {}
        )
    }
}
