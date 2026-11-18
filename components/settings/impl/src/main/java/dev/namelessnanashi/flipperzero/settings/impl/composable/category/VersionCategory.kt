package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.SimpleElement
import dev.namelessnanashi.flipperzero.settings.impl.viewmodels.VersionViewModel

private const val EXPERT_MODE_CLICK_COUNT = 10

@Composable
internal fun VersionCategory(
    versionViewModel: VersionViewModel,
    modifier: Modifier = Modifier,
    onActivateExpertMode: () -> Unit
) {
    val version = versionViewModel.versionApp()

    var howMuchClick by remember { mutableIntStateOf(0) }

    CardCategory(
        modifier = modifier.padding(bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    howMuchClick++
                    if (howMuchClick > EXPERT_MODE_CLICK_COUNT) {
                        onActivateExpertMode()
                    }
                }
                .padding(horizontal = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Image(
                painter = painterResource(R.drawable.pic_app_logo),
                contentDescription = null
            )
            SimpleElement(
                modifier = Modifier.weight(weight = 1f),
                title = stringResource(id = R.string.check_updates_text),
                description = stringResource(id = R.string.app_version_desc, version),
                titleTextStyle = LocalTypography.current.bodyR14
            )
        }
    }
}
