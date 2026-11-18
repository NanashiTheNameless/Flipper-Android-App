package dev.namelessnanashi.flipperzero.info.impl.compose.elements

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.info.impl.R
import dev.namelessnanashi.flipperzero.info.shared.ComposableOneRowCard

@Composable
fun ComposableScreenStreamingCard(
    modifier: Modifier = Modifier,
    onOpen: () -> Unit
) {
    ComposableOneRowCard(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 14.dp),
        iconId = R.drawable.ic_controller,
        titleId = R.string.info_device_screen_streaming_title,
        descriptionId = R.string.info_device_screen_streaming_desc,
        onOpen = onOpen
    )
}

@Preview(
    showBackground = true,
    showSystemUi = true
)
@Composable
private fun ComposableScreenStreamingCardPreview() {
    FlipperThemeInternal {
        ComposableScreenStreamingCard(onOpen = {})
    }
}
