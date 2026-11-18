package dev.namelessnanashi.flipperzero.keyscreen.shared

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableIButtonContent
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableInfraredContent
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableNFCContent
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableRFIDContent
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableSubGhzContent
import dev.namelessnanashi.flipperzero.keyscreen.shared.content.ComposableUnrecognizedContent

@Composable
fun ColumnScope.ComposableKeyContent(keyParsed: FlipperKeyParsed) {
    when (keyParsed) {
        is FlipperKeyParsed.RFID -> ComposableRFIDContent(keyParsed)
        is FlipperKeyParsed.IButton -> ComposableIButtonContent(keyParsed)
        is FlipperKeyParsed.Infrared -> ComposableInfraredContent()
        is FlipperKeyParsed.NFC -> ComposableNFCContent(keyParsed)
        is FlipperKeyParsed.SubGhz -> ComposableSubGhzContent(keyParsed)
        is FlipperKeyParsed.Unrecognized -> ComposableUnrecognizedContent(keyParsed)
    }
}
