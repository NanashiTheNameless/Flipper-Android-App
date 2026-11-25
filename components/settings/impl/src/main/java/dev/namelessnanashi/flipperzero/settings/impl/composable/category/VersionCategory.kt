package dev.namelessnanashi.flipperzero.settings.impl.composable.category

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.padding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalTypography
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.components.SimpleElement
import dev.namelessnanashi.flipperzero.settings.impl.viewmodels.VersionViewModel

// Fork: same flow as Android's developer options: 7 taps, countdown toasts from 4 left
private const val EXPERT_MODE_CLICK_COUNT = 7
private const val COUNTDOWN_FROM = 4

@Composable
internal fun VersionCategory(
    versionViewModel: VersionViewModel,
    isExpertMode: Boolean,
    modifier: Modifier = Modifier,
    onActivateExpertMode: () -> Unit
) {
    val version = versionViewModel.versionApp()

    var howMuchClick by remember { mutableIntStateOf(0) }
    val context = LocalContext.current
    var toast by remember { mutableStateOf<Toast?>(null) }
    fun showToast(text: String) {
        toast?.cancel()
        toast = Toast.makeText(context, text, Toast.LENGTH_SHORT).also { it.show() }
    }

    CardCategory(
        modifier = modifier.padding(bottom = 14.dp)
    ) {
        Row(
            modifier = Modifier
                .clickable {
                    if (isExpertMode) {
                        showToast(context.getString(R.string.expert_mode_already))
                        return@clickable
                    }
                    howMuchClick++
                    val left = EXPERT_MODE_CLICK_COUNT - howMuchClick
                    when {
                        left <= 0 -> {
                            howMuchClick = 0
                            onActivateExpertMode()
                            showToast(context.getString(R.string.expert_mode_enabled))
                        }
                        left <= COUNTDOWN_FROM -> showToast(
                            context.resources.getQuantityString(R.plurals.expert_mode_steps_left, left, left)
                        )
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
