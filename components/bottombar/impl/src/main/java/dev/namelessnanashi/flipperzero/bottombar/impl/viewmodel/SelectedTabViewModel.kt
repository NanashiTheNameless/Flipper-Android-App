package dev.namelessnanashi.flipperzero.bottombar.impl.viewmodel

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig.Apps
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig.Archive
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig.Device
import dev.namelessnanashi.flipperzero.bottombar.impl.model.BottomBarTabConfig.Tools
import dev.namelessnanashi.flipperzero.core.preference.pb.SelectedTab
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import dev.zacsweers.metro.Inject

/**
 * Needs to promote infrared remotes feature
 * On first update (not first launch) will return tools tab only one time
 */
class SelectedTabViewModel @Inject constructor(
    private val settingsDataStore: DataStore<Settings>,
) : DecomposeViewModel() {

    private fun toConfig(selectedTab: SelectedTab): BottomBarTabConfig {
        return when (selectedTab) {
            SelectedTab.DEVICE,
            is SelectedTab.Unrecognized -> Device(null)

            SelectedTab.ARCHIVE -> Archive(null)
            SelectedTab.APPS -> Apps(null)
            SelectedTab.TOOLS -> Tools(null)
        }
    }

    private fun setRemoteFeaturePromoted() {
        viewModelScope.launch {
            settingsDataStore.updateData { it.copy(infrared_remotes_tab_shown = true) }
        }
    }

    fun getSelectedTab(): BottomBarTabConfig {
        val settings = runBlocking { settingsDataStore.data.first() }
        if (settings.infrared_remotes_tab_shown) {
            return toConfig(settings.selected_tab)
        }
        // wasStartDialogShown indicates that flipper was already connected at least one time
        val wasStartDialogShown = settings.notification_dialog_shown
        if (!wasStartDialogShown) {
            setRemoteFeaturePromoted()
            return toConfig(settings.selected_tab)
        }
        setRemoteFeaturePromoted()
        return Tools(null)
    }
}
