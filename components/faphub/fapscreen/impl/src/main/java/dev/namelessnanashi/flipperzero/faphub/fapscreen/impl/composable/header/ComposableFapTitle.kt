package dev.namelessnanashi.flipperzero.faphub.fapscreen.impl.composable.header

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.placeholderConnecting
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.components.ComposableAppCategory
import dev.namelessnanashi.flipperzero.faphub.appcard.composable.components.ComposableAppIcon
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory

private val DEFAULT_NAME
    get() = String((Array(size = 10) { 'L' }).toCharArray())

@Composable
internal fun ComposableFapTitle(
    name: String?,
    iconUrl: String?,
    fapCategory: FapCategory?,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ComposableAppIcon(
            modifier = Modifier.size(64.dp),
            url = iconUrl,
            description = name
        )
        Column(
            Modifier.padding(start = 12.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                modifier = if (name == null) {
                    Modifier.placeholderConnecting()
                } else {
                    Modifier
                },
                text = name ?: DEFAULT_NAME,
                style = LocalTypography.current.titleM18,
                color = LocalPallet.current.text100,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            ComposableAppCategory(category = fapCategory)
        }
    }
}
