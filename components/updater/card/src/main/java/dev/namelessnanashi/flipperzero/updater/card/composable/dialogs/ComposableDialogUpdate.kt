package dev.namelessnanashi.flipperzero.updater.card.composable.dialogs

import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.dialog.composable.FlipperDialog
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.updater.card.R
import dev.namelessnanashi.flipperzero.updater.model.FirmwareChannel
import dev.namelessnanashi.flipperzero.updater.model.FirmwareVersion
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableSuccessfulUpdate(
    version: FirmwareVersion?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val imageId = DesignSystem.drawable.pic_update_successfull_dark

    FlipperDialog(
        modifier = modifier,
        painter = painterResource(imageId),
        titleComposable = {
            Text(
                text = stringResource(R.string.update_card_dialog_successful_title),
                textAlign = TextAlign.Center,
                style = LocalTypography.current.bodyM14,
                color = LocalPallet.current.successfullyColor
            )
        },
        buttonText = stringResource(R.string.update_card_dialog_successful_btn),
        onClickButton = onDismiss,
        onDismissRequest = onDismiss,
        textComposable = {
            Text(
                text = buildAnnotatedString {
                    append(annotatedStringWithVersion(version, LocalPallet.current.text100))
                    append(' ')
                    append(stringResource(R.string.update_card_dialog_successful_desc))
                },
                textAlign = TextAlign.Center,
                style = LocalTypography.current.bodyR14
            )
        }
    )
}

@Composable
fun ComposableFailedUpdate(
    version: FirmwareVersion?,
    onDismiss: () -> Unit,
    modifier: Modifier = Modifier
) {
    FlipperDialog(
        modifier = modifier,
        painter = painterResource(
            id = R.drawable.pic_update_failed_dark
        ),
        titleComposable = {
            Text(
                text = stringResource(R.string.update_card_dialog_failed_title),
                textAlign = TextAlign.Center,
                style = LocalTypography.current.bodyM14,
                color = LocalPallet.current.warningColor
            )
        },
        buttonText = stringResource(R.string.update_card_dialog_failed_btn),
        onClickButton = onDismiss,
        onDismissRequest = onDismiss,
        textComposable = {
            Text(
                text = buildAnnotatedString {
                    append(annotatedStringWithVersion(version, LocalPallet.current.text100))
                    append(' ')
                    append(stringResource(R.string.update_card_dialog_failed_desc))
                },
                textAlign = TextAlign.Center,
                style = LocalTypography.current.bodyR14
            )
        }
    )
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableSuccessfulUpdateWithoutVersionPreview() {
    FlipperThemeInternal {
        ComposableSuccessfulUpdate(version = null, {})
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableSuccessfulUpdatePreview() {
    FlipperThemeInternal {
        val version = FirmwareVersion(
            channel = FirmwareChannel.DEV,
            version = "1.7.8"
        )
        ComposableSuccessfulUpdate(version = version, {})
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableFailedUpdateWithoutVersionPreview() {
    FlipperThemeInternal {
        ComposableFailedUpdate(version = null, {})
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableFailedUpdateUpdatePreview() {
    FlipperThemeInternal {
        val version = FirmwareVersion(
            channel = FirmwareChannel.DEV,
            version = "1.7.8"
        )
        ComposableFailedUpdate(version = version, {})
    }
}
