package dev.namelessnanashi.flipperzero.ui.decompose.statusbar

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings

@Suppress("UNUSED_PARAMETER")
class ThemeStatusBarIconStyleProvider(dataStore: DataStore<Settings>) : StatusBarIconStyleProvider {
    override fun isStatusBarIconLight(systemIsDark: Boolean): Boolean = true
}
