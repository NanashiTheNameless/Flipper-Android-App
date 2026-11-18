package dev.namelessnanashi.flipperzero.bridge.connection.screens

import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bridge.connection.screens.device.ConnectionDeviceScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.bridge.connection.screens.models.ConnectionRootConfig
import dev.namelessnanashi.flipperzero.bridge.connection.screens.nopermission.ConnectionNoPermissionDecomposeComponent
import dev.namelessnanashi.flipperzero.bridge.connection.screens.search.ConnectionSearchDecomposeComponent
import dev.namelessnanashi.flipperzero.bridge.connection.screens.utils.PermissionChecker
import dev.namelessnanashi.flipperzero.filemanager.main.api.FileManagerDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class ConnectionRootDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    private val permissionChecker: PermissionChecker,
    private val searchDecomposeFactory: ConnectionSearchDecomposeComponent.Factory,
    private val connectionDeviceScreenDecomposeComponentFactory: ConnectionDeviceScreenDecomposeComponent.Factory,
    private val fileManagerDecomposeComponent: FileManagerDecomposeComponent.Factory
) : CompositeDecomposeComponent<ConnectionRootConfig>(), ComponentContext by componentContext {
    override val stack: Value<ChildStack<ConnectionRootConfig, DecomposeComponent>> = childStack(
        source = navigation,
        serializer = ConnectionRootConfig.serializer(),
        initialConfiguration = if (permissionChecker.isPermissionGranted()) {
            ConnectionRootConfig.Device
        } else {
            ConnectionRootConfig.NoPermission
        },
        childFactory = ::child,
        handleBackButton = true
    )

    private fun child(
        config: ConnectionRootConfig,
        componentContext: ComponentContext
    ): DecomposeComponent = when (config) {
        is ConnectionRootConfig.Search -> searchDecomposeFactory(
            componentContext = componentContext,
            onBack = navigation::pop
        )

        is ConnectionRootConfig.NoPermission ->
            ConnectionNoPermissionDecomposeComponent(componentContext)

        is ConnectionRootConfig.Device ->
            connectionDeviceScreenDecomposeComponentFactory(
                componentContext = componentContext,
                navigation = navigation
            )

        ConnectionRootConfig.FileManager -> fileManagerDecomposeComponent(
            componentContext = componentContext,
            onBack = navigation::pop
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext
        ): ConnectionRootDecomposeComponent
    }
}
