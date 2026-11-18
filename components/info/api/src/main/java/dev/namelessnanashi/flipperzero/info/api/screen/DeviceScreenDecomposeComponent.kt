package dev.namelessnanashi.flipperzero.info.api.screen

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.ui.decompose.CompositeDecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter

abstract class DeviceScreenDecomposeComponent<C : Any> : CompositeDecomposeComponent<C>() {
    abstract fun handleDeeplink(deeplink: Deeplink.BottomBar.DeviceTab)
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            deeplink: Deeplink.BottomBar.DeviceTab?,
            onBack: DecomposeOnBackParameter
        ): DeviceScreenDecomposeComponent<*>
    }
}
