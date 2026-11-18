package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import uk.kulikov.metro.assisted.AssistedKey

abstract class InfraredsScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {

    interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            brandId: Long,
            @AssistedKey("onBackClick") onBack: () -> Unit,
            onRemoteFound: (Long, String) -> Unit
        ): InfraredsScreenDecomposeComponent
    }
}
