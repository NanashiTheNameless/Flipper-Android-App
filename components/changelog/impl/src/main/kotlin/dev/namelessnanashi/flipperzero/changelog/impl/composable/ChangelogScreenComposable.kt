package dev.namelessnanashi.flipperzero.changelog.impl.composable

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.changelog.impl.R
import dev.namelessnanashi.flipperzero.core.markdown.ComposableMarkdown
import dev.namelessnanashi.flipperzero.core.ui.scrollbar.ComposableColumnScrollbarWithIndicator
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalRootNavigation
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.updater.model.UpdateRequest
import dev.namelessnanashi.flipperzero.updater.model.WebUpdaterFirmware

@Composable
fun ChangelogScreenComposable(
    updateRequest: UpdateRequest,
    changelog: String?,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val rootNavigation = LocalRootNavigation.current
    Column(
        modifier.fillMaxSize()
            .navigationBarsPadding()
    ) {
        ChangelogScreenBarComposable(
            firmwareVersion = updateRequest.updateTo,
            onBack = onBack
        )
        ComposableColumnScrollbarWithIndicator(
            modifier = Modifier
                .weight(1f)
                .padding(14.dp)
        ) {
            ComposableMarkdown(
                content = changelog ?: stringResource(R.string.whatsnew_empty),
                modifier = Modifier
                    .fillMaxSize()
                    .padding(4.dp)
            )
        }
        ChangelogUpdateButtonComposable(
            isUpdate = updateRequest.updateTo.channel == updateRequest.updateFrom.channel,
            onClick = {
                rootNavigation.push(RootScreenConfig.UpdateScreen(updateRequest))
            }
        )
    }
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ChangelogScreenComposablePreview() {
    FlipperThemeInternal {
        ChangelogScreenComposable(
            updateRequest = UpdateRequest(
                updateTo = FirmwareVersion(
                    channel = FirmwareChannel.RELEASE,
                    version = "0.99"
                ),
                updateFrom = FirmwareVersion(
                    channel = FirmwareChannel.DEV,
                    version = "0.99"
                ),
                changelog = "Hello world!",
                content = WebUpdaterFirmware("preview")
            ),
            changelog = "Hello world!",
            onBack = {}
        )
    }
}
