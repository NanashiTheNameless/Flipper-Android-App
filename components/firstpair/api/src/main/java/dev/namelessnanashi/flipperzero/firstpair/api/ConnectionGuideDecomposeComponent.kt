package dev.namelessnanashi.flipperzero.firstpair.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeOnBackParameter
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

/**
 * Fork: step-by-step guide for connecting a Flipper over Bluetooth and over a USB cable.
 */
abstract class ConnectionGuideDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {
    fun interface Factory {
        /**
         * @param onUsbConnected when set (first setup), the guide offers a "Use USB" button that
         * calls it once a Flipper is plugged in, so setup can finish without Bluetooth pairing
         */
        operator fun invoke(
            componentContext: ComponentContext,
            onBack: DecomposeOnBackParameter,
            onUsbConnected: (() -> Unit)?
        ): ConnectionGuideDecomposeComponent
    }
}
