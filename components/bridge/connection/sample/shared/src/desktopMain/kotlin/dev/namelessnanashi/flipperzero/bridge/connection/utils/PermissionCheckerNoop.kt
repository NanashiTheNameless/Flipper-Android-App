package dev.namelessnanashi.flipperzero.bridge.connection.utils

import dev.namelessnanashi.flipperzero.bridge.connection.screens.utils.PermissionChecker
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<PermissionChecker>())
class PermissionCheckerNoop @Inject constructor() : PermissionChecker {
    override fun isPermissionGranted() = true
}
