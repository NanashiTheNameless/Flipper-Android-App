package dev.namelessnanashi.flipperzero.faphub.appcard.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.components.ComposableAppCategory
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.components.ComposableAppIcon
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItem
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapItemShort

@Composable
fun ComposableAppDialogBox(
    fapItem: FapItem,
    modifier: Modifier = Modifier
) {
    ComposableAppDialogBox(
        name = fapItem.name,
        iconUrl = fapItem.picUrl,
        category = fapItem.category,
        modifier = modifier
    )
}

@Composable
fun ComposableAppDialogBox(
    fapItem: FapItemShort,
    modifier: Modifier = Modifier
) {
    ComposableAppDialogBox(
        name = fapItem.name,
        iconUrl = fapItem.picUrl,
        category = fapItem.category,
        modifier = modifier
    )
}

@Composable
fun ComposableAppDialogBox(
    name: String,
    iconUrl: String,
    category: FapCategory,
    modifier: Modifier = Modifier
) = Row(
    modifier = modifier
        .clip(RoundedCornerShape(12.dp))
        .background(LocalPallet.current.fapHubDeleteDialogBackground),
    horizontalArrangement = Arrangement.Center,
    verticalAlignment = Alignment.CenterVertically
) {
    ComposableAppIcon(
        modifier = Modifier
            .padding(vertical = 12.dp, horizontal = 8.dp)
            .size(42.dp),
        url = iconUrl,
        description = name
    )
    Column(
        verticalArrangement = Arrangement.spacedBy(1.dp, Alignment.CenterVertically)
    ) {
        Text(
            text = name,
            style = LocalTypography.current.bodyM14,
            color = LocalPallet.current.text100
        )
        ComposableAppCategory(category = category)
    }
}
