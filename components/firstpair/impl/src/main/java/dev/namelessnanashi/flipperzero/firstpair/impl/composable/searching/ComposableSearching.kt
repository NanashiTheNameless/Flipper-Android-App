package dev.namelessnanashi.flipperzero.firstpair.impl.composable.searching

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.bridge.api.scanner.DiscoveredBluetoothDevice
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.firstpair.impl.R
import dev.namelessnanashi.flipperzero.firstpair.impl.composable.common.ComposableSearchingAppBar
import dev.namelessnanashi.flipperzero.firstpair.impl.model.SearchingContent
import dev.namelessnanashi.flipperzero.firstpair.impl.model.SearchingState

@Composable
fun ComposableSearchingScreen(
    state: SearchingState,
    onBack: () -> Unit,
    onHelpClicking: () -> Unit,
    onSkipConnection: () -> Unit,
    onDeviceClick: (DiscoveredBluetoothDevice, resetPair: Boolean) -> Unit,
    onRefreshSearching: () -> Unit,
    onResetTimeoutState: () -> Unit,
    onUseUsb: () -> Unit,
    usbNotFound: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .background(LocalPallet.current.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        ComposableSearchingAppBar(stringResource(R.string.firstpair_search_title), onBack)
        ComposableSearchingStatus(state, onHelpClicking)
        // Fork: how to connect (the first screen is now the unofficial-fork notice)
        ConnectHint(R.string.firstpair_search_how_ble)
        ConnectHint(R.string.firstpair_search_how_usb)
        ComposableSearchingContent(
            modifier = Modifier.weight(weight = 1f),
            content = state.content,
            onDeviceClick = onDeviceClick,
            onRefreshSearching = onRefreshSearching,
            onResetTimeoutState = onResetTimeoutState
        )
        ComposableSearchingFooter(
            onClickSkipConnection = onSkipConnection,
            onUseUsb = onUseUsb,
            usbNotFound = usbNotFound
        )
    }
}

@Composable
private fun ConnectHint(@StringRes textId: Int) {
    Text(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp, vertical = 2.dp),
        text = stringResource(textId),
        style = LocalTypography.current.bodyR14,
        color = LocalPallet.current.text60
    )
}

@Composable
fun ComposableSearchingContent(
    content: SearchingContent,
    onDeviceClick: (DiscoveredBluetoothDevice, resetPair: Boolean) -> Unit,
    onRefreshSearching: () -> Unit,
    onResetTimeoutState: () -> Unit,
    modifier: Modifier = Modifier
) {
    when (content) {
        is SearchingContent.Finished -> Text(
            modifier = modifier.fillMaxWidth(),
            text = stringResource(R.string.firstpair_search_title),
            textAlign = TextAlign.Center
        )

        is SearchingContent.FoundedDevices -> ComposableSearchingDevices(
            modifier = modifier,
            state = content,
            onDeviceClick = onDeviceClick,
            onRefreshSearching = onRefreshSearching,
            onResetTimeoutState = onResetTimeoutState
        )

        is SearchingContent.PermissionRequest -> ComposablePermissionRequest(
            modifier = modifier,
            state = content
        )

        SearchingContent.Searching -> ComposableSearchingProgress(modifier)
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableSearchingScreenPreview() {
    ComposableSearchingScreen(
        SearchingState(
            showSearching = true,
            showHelp = true,
            content = SearchingContent.Searching
        ),
        onBack = {},
        onHelpClicking = {},
        onSkipConnection = {},
        onRefreshSearching = {},
        onDeviceClick = { _, _ -> },
        onResetTimeoutState = {},
        onUseUsb = {},
        usbNotFound = false
    )
}
