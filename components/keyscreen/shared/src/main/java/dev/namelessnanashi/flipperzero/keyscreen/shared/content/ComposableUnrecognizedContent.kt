package dev.namelessnanashi.flipperzero.keyscreen.shared.content

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed

@Composable
internal fun ColumnScope.ComposableUnrecognizedContent(
    unrecognizedKey: FlipperKeyParsed.Unrecognized
) {
    ComposableKeyContent(
        lines = unrecognizedKey.orderedDict
    )
}
