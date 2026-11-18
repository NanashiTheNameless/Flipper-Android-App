package dev.namelessnanashi.flipperzero.firstpair.impl.di

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.firstpair.impl.fragments.permissions.BluetoothEnableHelper
import dev.zacsweers.metro.ContributesTo

@ContributesTo(AppGraph::class)
interface FirstPairComponent {
    fun inject(bluetoothEnableHelper: BluetoothEnableHelper)
}
