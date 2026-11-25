package dev.namelessnanashi.flipperzero.firstpair.impl.composable.searching

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.clickableRipple
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.firstpair.impl.R

@Composable
fun ComposableSearchingFooter(
    onClickSkipConnection: () -> Unit,
    onUseUsb: () -> Unit,
    usbNotFound: Boolean,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.padding(horizontal = 14.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // Fork: USB needs no pairing; finish setup over the cable
        ComposableFlipperButton(
            modifier = Modifier
                .padding(top = 8.dp)
                .fillMaxWidth(),
            text = stringResource(R.string.firstpair_search_use_usb),
            textStyle = LocalTypography.current.buttonB16,
            textPadding = PaddingValues(vertical = 12.dp, horizontal = 24.dp),
            onClick = onUseUsb
        )
        if (usbNotFound) {
            Text(
                modifier = Modifier
                    .padding(top = 6.dp)
                    .fillMaxWidth(),
                text = stringResource(R.string.firstpair_guide_usb_not_found),
                style = LocalTypography.current.bodyR14,
                color = LocalPallet.current.text60,
                textAlign = TextAlign.Center
            )
        }
        SkipConnectionText(onClickSkipConnection)
    }
}

@Composable
private fun SkipConnectionText(
    onClickSkipConnection: () -> Unit,
    modifier: Modifier = Modifier
) {
    Text(
        modifier = modifier
            .clickableRipple(onClick = onClickSkipConnection)
            .padding(all = 8.dp)
            .fillMaxWidth(),
        text = stringResource(R.string.firstpair_search_skip_connection),
        color = LocalPallet.current.accentSecond,
        style = LocalTypography.current.buttonM16,
        textAlign = TextAlign.Center
    )
}
