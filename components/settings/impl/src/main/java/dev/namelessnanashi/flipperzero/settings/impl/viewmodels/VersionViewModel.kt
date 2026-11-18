package dev.namelessnanashi.flipperzero.settings.impl.viewmodels

import dev.namelessnanashi.flipperzero.core.di.ApplicationParams
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.zacsweers.metro.Inject

class VersionViewModel @Inject constructor(
    private val applicationParams: ApplicationParams
) : DecomposeViewModel() {
    fun versionApp() = applicationParams.version
}
