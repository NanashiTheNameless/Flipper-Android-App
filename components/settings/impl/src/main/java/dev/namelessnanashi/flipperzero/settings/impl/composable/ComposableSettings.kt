package dev.namelessnanashi.flipperzero.settings.impl.composable

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import dev.namelessnanashi.flipperzero.core.ui.ktx.OrangeAppBar
import dev.namelessnanashi.flipperzero.core.ui.theme.LocalPallet
import dev.namelessnanashi.flipperzero.settings.impl.R
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.AppCategory
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.DebugCategory
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.ExperimentalCategory
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.ExportKeysCategory
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.OtherSettingsCategory
import dev.namelessnanashi.flipperzero.settings.impl.composable.category.VersionCategory
import dev.namelessnanashi.flipperzero.settings.impl.model.DebugSettingAction
import dev.namelessnanashi.flipperzero.settings.impl.model.SettingsNavigationConfig
import dev.namelessnanashi.flipperzero.settings.impl.viewmodels.DebugViewModel
import dev.namelessnanashi.flipperzero.settings.impl.viewmodels.SettingsViewModel
import dev.namelessnanashi.flipperzero.settings.impl.viewmodels.VersionViewModel

@Composable
@Suppress("NonSkippableComposable")
fun ComposableSettings(
    settingsViewModel: SettingsViewModel,
    debugViewModel: DebugViewModel,
    versionViewModel: VersionViewModel,
    onOpen: (SettingsNavigationConfig) -> Unit,
    onDebugAction: (DebugSettingAction) -> Unit,
    modifier: Modifier = Modifier,
    onBack: () -> Unit
) {
    val context = LocalContext.current

    val settings by settingsViewModel.getState().collectAsState()
    val exportState by settingsViewModel.getExportState().collectAsState()

    SafeStatusBarBox(modifier = modifier) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .background(LocalPallet.current.background),
            verticalArrangement = Arrangement.spacedBy(14.dp)
        ) {
            OrangeAppBar(R.string.options, onBack = onBack)
            AppCategory(
                theme = settings.selected_theme,
                onSelectTheme = settingsViewModel::onChangeSelectedTheme,
                vibrationState = !settings.disabled_vibration,
                onSwitchVibration = settingsViewModel::onSwitchVibration
            )
            if (settings.expert_mode) {
                DebugCategory(
                    settings = settings,
                    onSwitchDebug = settingsViewModel::onSwitchDebug,
                    onAction = onDebugAction,
                    onDebugSettingSwitch = debugViewModel::onSwitch
                )
            }
            ExperimentalCategory(
                settings = settings,
                onSwitchExperimental = settingsViewModel::onSwitchExperimental,
                onOpenFM = { onOpen(SettingsNavigationConfig.FileManager) },
            )
            ExportKeysCategory(
                exportState = exportState,
                onExport = { settingsViewModel.onMakeExport(context) }
            )
            OtherSettingsCategory()
            @Suppress("ViewModelForwarding")
            VersionCategory(
                onActivateExpertMode = settingsViewModel::onExpertModeActivate,
                versionViewModel = versionViewModel
            )
        }
    }
}

@Composable
private fun SafeStatusBarBox(
    modifier: Modifier = Modifier,
    statusBarColor: Color = LocalPallet.current.appBar,
    backgroundColor: Color = LocalPallet.current.background,
    content: @Composable () -> Unit
) {
    Box(
        modifier = modifier
            .fillMaxSize()
            .background(statusBarColor)
            .statusBarsPadding()
            .background(backgroundColor)
    ) {
        // Used to fill overflow color on top of StatusBars
        if (statusBarColor != backgroundColor) {
            Box(
                modifier = Modifier
                    .background(statusBarColor)
                    .statusBarsPadding()
                    .fillMaxWidth()
                    .padding(vertical = 20.dp)
            )
        }
        content.invoke()
    }
}
