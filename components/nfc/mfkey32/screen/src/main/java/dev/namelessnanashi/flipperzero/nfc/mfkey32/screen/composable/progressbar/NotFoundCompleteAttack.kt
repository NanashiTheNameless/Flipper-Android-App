package dev.namelessnanashi.flipperzero.nfc.mfkey32.screen.composable.progressbar

import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.elements.ComposableFlipperButton
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.nfc.mfkey32.screen.R

@Composable
fun NotFoundCompleteAttack(
    onComplete: () -> Unit,
    modifier: Modifier = Modifier,
) = Column(
    modifier.fillMaxWidth(),
    horizontalAlignment = Alignment.CenterHorizontally
) {
    Text(
        modifier = Modifier.padding(18.dp),
        text = stringResource(R.string.mfkey32_complete_not_found_title),
        style = LocalTypography.current.titleB18,
        color = LocalPallet.current.text100
    )
    Image(
        modifier = Modifier.padding(horizontal = 18.dp),
        painter = painterResource(
            R.drawable.pic_shrug_black
        ),
        contentDescription = stringResource(R.string.mfkey32_complete_not_found_title)
    )
    ComposableFlipperButton(
        modifier = Modifier
            .padding(horizontal = 24.dp, vertical = 24.dp)
            .fillMaxWidth(),
        text = stringResource(R.string.mfkey32_complete_btn),
        textPadding = PaddingValues(vertical = 14.dp),
        onClick = onComplete
    )
}

@Preview(
    showSystemUi = true,
    showBackground = true
)
@Composable
private fun ComposableNotFoundCompleteAttackPreview() {
    FlipperThemeInternal {
        Box {
            NotFoundCompleteAttack({})
        }
    }
}
