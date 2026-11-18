package dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.di

import android.content.Context
import dev.namelessnanashi.flipperzero.bridge.connection.feature.actionnotifier.api.FlipperActionNotifier
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.di.DeviceConnectionApiHolder
import dev.namelessnanashi.flipperzero.bridge.connection.transport.common.api.di.toHolder
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUSBDeviceConnectionConfig
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.impl.AndroidUSBDeviceConnectionApiImpl
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.zacsweers.metro.ClassKey
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.IntoMap
import dev.zacsweers.metro.Provides

@ContributesTo(AppGraph::class)
interface USBDeviceConnectionModule {
    @Provides
    @IntoMap
    @ClassKey(FUSBDeviceConnectionConfig::class)
    fun provideUsbDeviceConnectionApi(
        context: Context,
        actionNotifierFactory: FlipperActionNotifier.Factory
    ): DeviceConnectionApiHolder = AndroidUSBDeviceConnectionApiImpl(context, actionNotifierFactory).toHolder()
}
