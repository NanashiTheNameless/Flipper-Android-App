package dev.namelessnanashi.flipperzero.keyscreen.shared.bar

import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.keyscreen.shared.R
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableBarBackIcon(modifier: Modifier = Modifier, onClick: (() -> Unit)? = null) {
    var clickableModifier = modifier
    if (onClick != null) {
        clickableModifier = clickableModifier.clickableRipple(bounded = false, onClick = onClick)
    }

    Icon(
        modifier = clickableModifier
            .size(24.dp),
        painter = painterResource(DesignSystem.drawable.ic_back),
        contentDescription = stringResource(R.string.keyscreen_back_pic_desc)
    )
}
