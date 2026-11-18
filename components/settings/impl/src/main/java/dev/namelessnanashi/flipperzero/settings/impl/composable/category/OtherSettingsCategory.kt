package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.GrayDivider
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.UrlElement

@Composable
fun OtherSettingsCategory() {
    CardCategory {
        UrlElement(
            iconId = R.drawable.ic_forum,
            titleId = R.string.other_forum_open,
            url = stringResource(R.string.other_forum_url)
        )
        GrayDivider()
        UrlElement(
            iconId = R.drawable.ic_github,
            titleId = R.string.other_github_open,
            url = stringResource(R.string.other_github_url)
        )
    }
}
