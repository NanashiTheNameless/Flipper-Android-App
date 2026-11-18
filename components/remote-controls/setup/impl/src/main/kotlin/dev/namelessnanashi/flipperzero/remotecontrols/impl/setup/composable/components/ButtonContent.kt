package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.composable.components

import android.content.res.Configuration
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.button.ButtonItemComposable
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.ButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.TextButtonData

@Composable
private fun SignalResponseButton(
    data: ButtonData,
    onClick: () -> Unit,
    emulatedKeyIdentifier: IfrKeyIdentifier?,
    isSyncing: Boolean,
    isConnected: Boolean
) {
    ButtonItemComposable(
        buttonData = data,
        onKeyDataClick = { _, _ -> onClick.invoke() },
        modifier = Modifier.size(64.dp),
        emulatedKeyIdentifier = emulatedKeyIdentifier,
        isSyncing = isSyncing,
        isConnected = isConnected,
    )
}

@Composable
fun ButtonContent(
    onClick: () -> Unit,
    data: ButtonData,
    isSyncing: Boolean,
    isConnected: Boolean,
    emulatedKeyIdentifier: IfrKeyIdentifier?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        SignalResponseButton(
            data = data,
            onClick = onClick,
            emulatedKeyIdentifier = emulatedKeyIdentifier,
            isSyncing = isSyncing,
            isConnected = isConnected,
        )
    }
}

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
private fun ComposableConfirmContentDarkPreview() {
    FlipperThemeInternal {
        Column {
            ButtonContent(
                onClick = {},
                data = TextButtonData(text = "Hello"),
                emulatedKeyIdentifier = null,
                isSyncing = false,
                isConnected = true
            )
            ButtonContent(
                onClick = {},
                data = TextButtonData(text = "TV/AV"),
                emulatedKeyIdentifier = null,
                isSyncing = false,
                isConnected = true
            )
            ButtonContent(
                onClick = {},
                data = TextButtonData(text = "Hello world"),
                emulatedKeyIdentifier = null,
                isSyncing = false,
                isConnected = true
            )
        }
    }
}
