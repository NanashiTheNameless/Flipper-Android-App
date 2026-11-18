package dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.composable.util

import dev.namelessnanashi.flipperzero.remotecontrols.impl.grid.remote.presentation.decompose.RemoteGridComponent

internal val RemoteGridComponent.Model.contentKey: Any
    get() = when (this) {
        is RemoteGridComponent.Model.Error -> 0
        is RemoteGridComponent.Model.Loaded -> 1
        is RemoteGridComponent.Model.Loading -> 2
    }
