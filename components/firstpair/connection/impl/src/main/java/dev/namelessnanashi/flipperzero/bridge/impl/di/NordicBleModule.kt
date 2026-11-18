package dev.namelessnanashi.flipperzero.bridge.impl.di

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import no.nordicsemi.android.support.v18.scanner.BluetoothLeScannerCompat

@ContributesTo(AppGraph::class)
interface NordicBleModule {
    @Provides
    fun provideBluetoothLeScanner(): BluetoothLeScannerCompat {
        return BluetoothLeScannerCompat.getScanner()
    }
}
