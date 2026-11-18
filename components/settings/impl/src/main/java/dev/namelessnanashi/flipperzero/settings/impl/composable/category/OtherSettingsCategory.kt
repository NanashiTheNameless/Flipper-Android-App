package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.UrlElement

@Composable
fun OtherSettingsCategory() {
    CardCategory {
        UrlElement(
            iconId = R.drawable.ic_github,
            titleId = R.string.other_github_open,
            url = stringResource(R.string.other_github_url)
        )
    }
}
