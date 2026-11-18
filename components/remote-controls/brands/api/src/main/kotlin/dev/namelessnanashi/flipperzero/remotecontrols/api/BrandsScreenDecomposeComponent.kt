package dev.namelessnanashi.flipperzero.remotecontrols.api

import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent

abstract class BrandsScreenDecomposeComponent(
    componentContext: ComponentContext
) : ScreenDecomposeComponent(componentContext) {

    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            categoryId: Long,
            onBackClick: () -> Unit,
            onBrandClick: (brandId: Long, brandName: String) -> Unit,
            onBrandLongClick: (brandId: Long) -> Unit
        ): BrandsScreenDecomposeComponent
    }
}
