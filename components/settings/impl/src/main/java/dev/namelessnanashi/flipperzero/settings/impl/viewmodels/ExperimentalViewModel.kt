package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.settings.impl.model.SettingsNavigationConfig
import dev.zacsweers.metro.Inject

class ExperimentalViewModel @Inject constructor() : DecomposeViewModel() {
    fun onOpenFileManager(navigation: StackNavigation<SettingsNavigationConfig>) {
        navigation.pushToFront(SettingsNavigationConfig.FileManager)
    }
}
