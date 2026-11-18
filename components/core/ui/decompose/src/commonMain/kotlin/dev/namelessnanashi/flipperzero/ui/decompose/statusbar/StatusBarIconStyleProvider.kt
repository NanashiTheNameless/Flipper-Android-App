package dev.namelessnanashi.flipperzero.ui.decompose.statusbar

internal interface StatusBarIconStyleProvider {
    /**
     * @return null to keep the icon style SingleActivity sets from the app theme
     */
    fun isStatusBarIconLight(systemIsDark: Boolean): Boolean?
}
