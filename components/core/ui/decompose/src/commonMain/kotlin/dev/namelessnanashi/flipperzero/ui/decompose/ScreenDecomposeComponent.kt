package dev.namelessnanashi.flipperzero.ui.decompose

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.essenty.lifecycle.Lifecycle
import dev.namelessnanashi.flipperzero.ui.decompose.internal.WindowDecorator
import dev.namelessnanashi.flipperzero.ui.decompose.internal.createWindowDecorator
import dev.namelessnanashi.flipperzero.ui.decompose.statusbar.StatusBarIconStyleProvider

abstract class ScreenDecomposeComponent(
    componentContext: ComponentContext
) : DecomposeComponent(),
    ComponentContext by componentContext,
    Lifecycle.Callbacks,
    StatusBarIconStyleProvider {
    private val windowDecorator: WindowDecorator by lazy {
        createWindowDecorator(statusBarIconStyleProvider = this)
    }

    init {
        lifecycle.subscribe(this)
    }

    override fun onResume() {
        super.onResume()
        windowDecorator.decorate()
    }

    // Upstream forced dark icons for its orange headers; headers now follow the theme
    override fun isStatusBarIconLight(systemIsDark: Boolean): Boolean? = null
}
