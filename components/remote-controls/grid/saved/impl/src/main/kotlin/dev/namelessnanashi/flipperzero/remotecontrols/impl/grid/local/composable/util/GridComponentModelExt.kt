package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.composable.util

import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.local.presentation.decompose.LocalGridComponent

internal val LocalGridComponent.Model.contentKey: Any
    get() = when (this) {
        LocalGridComponent.Model.Error -> 0
        is LocalGridComponent.Model.Loaded -> 1
        is LocalGridComponent.Model.Loading -> 2
    }
