package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import androidx.compose.foundation.layout.padding
import androidx.compose.material.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.preference.pb.SelectedTheme
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.GrayDivider
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.SwitchableElement
import dev.namelessnanashi.flipperzero.settings.impl.composable.elements.ThemeChangerElement

@Composable
fun AppCategory(
    theme: SelectedTheme,
    onSelectTheme: (SelectedTheme) -> Unit,
    vibrationState: Boolean,
    onSwitchVibration: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    CardCategory(modifier = modifier) {
        Text(
            modifier = Modifier.padding(all = 12.dp),
            text = stringResource(R.string.app_title),
            style = LocalTypography.current.buttonB16
        )
        ThemeChangerElement(
            theme = theme,
            onSelectTheme = onSelectTheme
        )
        GrayDivider()
        SwitchableElement(
            state = vibrationState,
            titleId = R.string.app_vibration_title,
            descriptionId = R.string.app_vibration_desc,
            onSwitchState = onSwitchVibration
        )
    }
}
