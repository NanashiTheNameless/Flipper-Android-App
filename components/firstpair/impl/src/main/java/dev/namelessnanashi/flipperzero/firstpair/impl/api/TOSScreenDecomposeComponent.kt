package dev.namelessnanashi.flipperzero.firstpair.impl.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import dev.namelessnanashi.flipperzero.firstpair.impl.composable.tos.ComposableTOS
import dev.namelessnanashi.flipperzero.firstpair.impl.storage.FirstPairStorage
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class TOSScreenDecomposeComponent @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val onApply: () -> Unit,
    private val firstPairStorage: FirstPairStorage
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        ComposableTOS(
            onApplyPress = {
                firstPairStorage.markTosPassed()
                onApply()
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            onApply: () -> Unit,
        ): TOSScreenDecomposeComponent
    }
}
