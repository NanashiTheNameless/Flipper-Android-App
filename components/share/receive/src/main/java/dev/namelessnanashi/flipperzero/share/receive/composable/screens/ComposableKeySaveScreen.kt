package dev.namelessnanashi.flipperzero.share.receive.composable.screens

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyscreen.api.KeyScreenApi
import dev.namelessnanashi.flipperzero.share.receive.R
import dev.namelessnanashi.flipperzero.share.receive.composable.components.ComposableKeySaveBar
import dev.namelessnanashi.flipperzero.share.receive.composable.components.ComposableKeySaveFooter

@Composable
fun ComposableKeySaveScreen(
    keyScreenApi: KeyScreenApi,
    keyParsed: FlipperKeyParsed,
    savingInProgress: Boolean,
    onSave: () -> Unit,
    onCancel: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier
    ) {
        ComposableKeySaveBar(onCancel)
        keyScreenApi.KeyCard(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(start = 24.dp, end = 24.dp, bottom = 12.dp),
            key = keyParsed,
            deleted = false
        )
        ComposableKeySaveFooter {
            if (savingInProgress) {
                Box(
                    modifier = Modifier
                        .padding(32.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            } else {
                ComposableFlipperButton(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(top = 12.dp, bottom = 24.dp),
                    text = stringResource(R.string.receive_save_btn),
                    onClick = onSave
                )
            }
        }
    }
}
