package dev.namelessnanashi.flipperzero.share.receive.composable.screens

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.LocalPlaceholderEnable
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyScreenApi
import dev.namelessnanashi.flipperzero.share.receive.R
import dev.namelessnanashi.flipperzero.share.receive.composable.components.ComposableKeySaveBar
import dev.namelessnanashi.flipperzero.share.receive.composable.components.ComposableKeySaveFooter
import kotlinx.collections.immutable.persistentListOf

@Composable
internal fun ComposableKeyInProgressScreen(keyScreenApi: KeyScreenApi, onCancel: () -> Unit) {
    val keyParsed = FlipperKeyParsed.Unrecognized(
        keyName = "",
        notes = "",
        fileType = null,
        orderedDict = persistentListOf("" to "")
    )

    Column {
        ComposableKeySaveBar(onCancel)
        CompositionLocalProvider(LocalPlaceholderEnable provides true) {
            keyScreenApi.KeyCard(
                modifier = Modifier.padding(start = 24.dp, end = 24.dp, bottom = 24.dp),
                key = keyParsed,
                deleted = false
            )
            ComposableKeySaveFooter {
                ComposableFlipperButton(
                    modifier = Modifier.fillMaxWidth(),
                    text = stringResource(R.string.receive_save_btn),
                    onClick = {}
                )
            }
        }
    }
}
