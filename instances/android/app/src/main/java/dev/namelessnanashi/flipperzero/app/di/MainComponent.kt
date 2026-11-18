package dev.namelessnanashi.flipperzero.app.di

import dev.namelessnanashi.flipperzero.app.background.ConnectionKeepAlive
import dev.namelessnanashi.flipperzero.bridge.connection.service.api.FConnectionService
import dev.namelessnanashi.flipperzero.bridge.connection.transport.usb.api.FUsbFlipperSwitcherApi
import dev.namelessnanashi.flipperzero.bridge.synchronization.api.SynchronizationApi
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.permission.impl.PermissionRequestHandlerImpl
import dev.namelessnanashi.flipperzero.app.warnings.OtherFlipperAppWarning
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provider

@ContributesTo(AppGraph::class)
interface MainComponent {
    val synchronizationApi: Provider<SynchronizationApi>
    val permissionRequestHandlerImpl: Provider<PermissionRequestHandlerImpl>
    val fConnectionService: Provider<FConnectionService>
    val usbFlipperSwitcher: Provider<FUsbFlipperSwitcherApi>
    val otherFlipperAppWarning: Provider<OtherFlipperAppWarning>
    val connectionKeepAlive: Provider<ConnectionKeepAlive>
}
