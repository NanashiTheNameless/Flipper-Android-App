package dev.namelessnanashi.flipperzero.keyscreen.shared.content

import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyscreen.shared.R
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ColumnScope.ComposableSubGhzContent(subghz: FlipperKeyParsed.SubGhz) {
    ComposableKeyContent(
        lines = persistentListOf(
            stringResource(R.string.content_subghz_protocol) to subghz.protocol,
            stringResource(R.string.content_subghz_key) to subghz.key
        )
    )
}
