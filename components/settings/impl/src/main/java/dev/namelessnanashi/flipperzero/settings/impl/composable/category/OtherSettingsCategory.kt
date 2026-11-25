package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.CategoryElement
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.ClickableElement
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.UrlElement

@Composable
fun OtherSettingsCategory(
    showForkNotice: Boolean,
    onSwitchForkNotice: (Boolean) -> Unit,
    onOpenConnectionGuide: () -> Unit
) {
    CardCategory {
        CategoryElement(
            titleId = R.string.other_fork_notice,
            descriptionId = R.string.other_fork_notice_desc,
            state = showForkNotice,
            onSwitchState = onSwitchForkNotice
        )
        ClickableElement(
            titleId = R.string.other_connection_guide,
            descriptionId = R.string.other_connection_guide_desc,
            onClick = onOpenConnectionGuide
        )
        UrlElement(
            iconId = R.drawable.ic_github,
            titleId = R.string.other_github_open,
            url = stringResource(R.string.other_github_url)
        )
    }
}
