package dev.namelessnanashi.flipperzero.bridge.connection.di

import dev.namelessnanashi.flipperzero.bridge.connection.screens.ConnectionRootDecomposeComponent
import dev.namelessnanashi.flipperzero.core.ui.theme.viewmodel.ThemeViewModel
import dev.zacsweers.metro.Provider

interface AppComponent {
    val themeViewModelProvider: Provider<ThemeViewModel>
    val rootComponentFactory: ConnectionRootDecomposeComponent.Factory
}
