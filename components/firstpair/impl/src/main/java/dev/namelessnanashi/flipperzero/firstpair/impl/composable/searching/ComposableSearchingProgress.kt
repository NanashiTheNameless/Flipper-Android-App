package dev.namelessnanashi.flipperzero.firstpair.impl.composable.searching

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Icon
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.image.ComposeLottiePic
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.firstpair.impl.R
import dev.namelessnanashi.flipperzero.core.ui.res.R as DesignSystem

@Composable
fun ComposableSearchingProgress(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier,
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Row(
            modifier = Modifier.padding(bottom = 36.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                modifier = Modifier
                    .size(size = 82.dp)
                    .padding(end = 13.dp),
                painter = painterResource(R.drawable.pic_phone),
                contentDescription = null
            )
            ComposeLottiePic(
                picModifier = Modifier.size(size = 32.dp),
                picResId = DesignSystem.raw.dots_loader,
                rollBackPicResId = DesignSystem.drawable.pic_loader
            )
            Icon(
                modifier = Modifier
                    .size(size = 82.dp)
                    .padding(start = 13.dp),
                painter = painterResource(R.drawable.pic_flipper_heavy),
                contentDescription = stringResource(R.string.firstpair_search_flipper_status),
                tint = LocalPallet.current.accentSecond
            )
        }

        Text(
            text = stringResource(R.string.firstpair_search_loader_text),
            color = LocalPallet.current.text30,
            style = LocalTypography.current.titleR18
        )
    }
}
