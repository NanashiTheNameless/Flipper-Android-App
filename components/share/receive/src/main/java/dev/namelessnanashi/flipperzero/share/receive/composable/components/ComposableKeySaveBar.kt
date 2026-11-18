package dev.namelessnanashi.flipperzero.share.receive.composable.components

import androidx.compose.runtime.Composable
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarCancelIcon
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableBarTitle
import dev.namelessnanashi.flipperzero.keyscreen.shared.bar.ComposableKeyScreenAppBar
import dev.namelessnanashi.flipperzero.share.receive.R

@Composable
internal fun ComposableKeySaveBar(onBack: () -> Unit) {
    ComposableKeyScreenAppBar(
        centerBlock = { ComposableBarTitle(modifier = it, textId = R.string.receive_title) },
        endBlock = { ComposableBarCancelIcon(modifier = it, onClick = onBack) }
    )
}
