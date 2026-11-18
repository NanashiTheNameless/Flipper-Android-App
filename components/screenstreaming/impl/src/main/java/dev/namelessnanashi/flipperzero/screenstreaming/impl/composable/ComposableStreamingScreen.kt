package dev.namelessnanashi.flipperzero.screenstreaming.impl.composable

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialog
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import com.flipperdevices.protobuf.screen.InputType
import dev.namelessnanashi.flipperzero.screenstreaming.impl.R
import dev.namelessnanashi.flipperzero.screenstreaming.impl.composable.controls.ComposableFlipperControls
import dev.namelessnanashi.flipperzero.screenstreaming.impl.composable.screen.ComposableFlipperScreenWithOptions
import dev.namelessnanashi.flipperzero.screenstreaming.impl.model.FlipperLockState
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.ScreenStreamingViewModel
import dev.namelessnanashi.flipperzero.screenstreaming.impl.viewmodel.ScreenshotViewModel
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.OutlinedTextField
import androidx.compose.material.Text
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import kotlinx.collections.immutable.toImmutableList
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@ExperimentalFoundationApi
@ExperimentalComposeUiApi
@Composable
fun ComposableStreamingScreen(
    screenStreamingViewModel: ScreenStreamingViewModel,
    screenshotViewModel: ScreenshotViewModel,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val flipperScreen by screenStreamingViewModel.getFlipperScreen().collectAsState()
    val buttons by screenStreamingViewModel.getFlipperButtons().collectAsState()
    val lockState by screenStreamingViewModel.getLockState().collectAsState()
    val keyboardError by screenStreamingViewModel.getKeyboardError().collectAsState()
    var showKeyboardDialog by remember { mutableStateOf(false) }
    if (showKeyboardDialog) {
        ComposableKeyboardDialog(
            onSend = { text ->
                showKeyboardDialog = false
                screenStreamingViewModel.onSendText(text)
            },
            onDismiss = { showKeyboardDialog = false }
        )
    }
    if (keyboardError) {
        FlipperDialog(
            buttonText = stringResource(R.string.control_options_dialog_btn),
            onClickButton = screenStreamingViewModel::onDismissKeyboardError,
            onDismissRequest = screenStreamingViewModel::onDismissKeyboardError,
            text = stringResource(R.string.control_keyboard_failed)
        )
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(LocalPallet.current.background)
            .navigationBarsPadding(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        OrangeAppBar(
            titleId = R.string.control_title,
            onBack = onBack
        )
        var showDeprecatedLockDialog by remember { mutableStateOf(false) }
        if (showDeprecatedLockDialog) {
            ComposableDeprecatedLockDialog(onDismiss = { showDeprecatedLockDialog = false })
        }
        ComposableFlipperScreenWithOptions(
            modifier = Modifier.weight(1f),
            flipperScreen = flipperScreen,
            buttons = buttons.toImmutableList(),
            onTakeScreenshot = { screenshotViewModel.shareScreenshot(flipperScreen) },
            onOpenKeyboard = { showKeyboardDialog = true },
            lockState = lockState,
            onClickLockButton = {
                lockState.let {
                    when (it) {
                        FlipperLockState.NotInitialized -> {}
                        FlipperLockState.NotSupported -> showDeprecatedLockDialog = true
                        is FlipperLockState.Ready -> screenStreamingViewModel.onChangeLock(!it.isLocked)
                    }
                }
            }
        )
        ComposableFlipperControls(
            onPressButton = {
                screenStreamingViewModel.onPressButton(it, InputType.SHORT)
            },
            onLongPressButton = {
                screenStreamingViewModel.onPressButton(it, InputType.LONG)
            }
        )
    }
}

@Composable
private fun ComposableDeprecatedLockDialog(
    onDismiss: () -> Unit
) {
    FlipperDialog(
        buttonText = stringResource(R.string.control_options_dialog_btn),
        onClickButton = onDismiss,
        onDismissRequest = onDismiss,
        title = stringResource(R.string.control_options_dialog_title),
        text = stringResource(R.string.control_options_dialog_desc),
        imageComposable = {
            Image(
                modifier = Modifier.size(size = 96.dp),
                painter = painterResource(DesignSystem.drawable.ic_firmware_flipper_deprecated),
                contentDescription = stringResource(R.string.control_options_dialog_title)
            )
        }
    )
}

@Composable
private fun ComposableKeyboardDialog(
    onSend: (String) -> Unit,
    onDismiss: () -> Unit
) {
    var text by remember { mutableStateOf("") }
    FlipperDialog(
        title = {
            Text(
                text = stringResource(R.string.control_keyboard_title),
                style = LocalTypography.current.bodyM14,
                color = LocalPallet.current.text100
            )
        },
        text = {
            OutlinedTextField(
                modifier = Modifier.fillMaxWidth(),
                value = text,
                onValueChange = { text = it },
                singleLine = false,
                placeholder = { Text(stringResource(R.string.control_keyboard_hint)) }
            )
        },
        buttons = {
            ComposableFlipperButton(
                modifier = Modifier.fillMaxWidth(),
                text = stringResource(R.string.control_keyboard_send),
                onClick = { onSend(text) },
                textPadding = PaddingValues(12.dp)
            )
        },
        onDismissRequest = onDismiss
    )
}
