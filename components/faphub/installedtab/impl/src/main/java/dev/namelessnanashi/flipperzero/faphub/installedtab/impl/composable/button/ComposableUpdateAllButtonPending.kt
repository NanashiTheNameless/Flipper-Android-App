package dev.namelessnanashi.flipperzero.faphub.installedtab.impl.composable.button

import androidx.compose.foundation.border
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.installedtab.impl.R

@Composable
fun ComposableUpdateAllButtonPending(
    pendingCount: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(LocalPallet.current.background)
            .border(1.5.dp, LocalPallet.current.updateProgressGreen, RoundedCornerShape(8.dp)),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.Center
    ) {
        Text(
            modifier = Modifier.padding(start = 12.dp, top = 9.dp, bottom = 9.dp, end = 12.dp),
            text = stringResource(R.string.faphub_installed_update_all),
            textAlign = TextAlign.Center,
            style = LocalTypography.current.fapHubButtonText.copy(
                fontSize = 18.sp
            ),
            color = LocalPallet.current.updateProgressGreen
        )

        Box(
            modifier = Modifier
                .padding(vertical = 8.dp)
                .clip(RoundedCornerShape(4.dp))
                .background(LocalPallet.current.background)
                .border(1.dp, LocalPallet.current.updateProgressGreen, RoundedCornerShape(4.dp))
        ) {
            Text(
                modifier = Modifier.padding(vertical = 2.dp, horizontal = 7.dp),
                text = pendingCount.toString(),
                style = LocalTypography.current.titleB18,
                textAlign = TextAlign.Center,
                color = LocalPallet.current.updateProgressGreen
            )
        }
    }
}
