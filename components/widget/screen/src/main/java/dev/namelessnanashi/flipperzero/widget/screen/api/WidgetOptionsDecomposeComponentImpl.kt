package dev.namelessnanashi.flipperzero.widget.screen.api

import androidx.compose.runtime.Composable
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.archive.api.ArchiveApi
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.namelessnanashi.flipperzero.widget.screen.compose.WidgetOptionsComposable
import dev.namelessnanashi.flipperzero.widget.screen.model.WidgetNavigationConfig
import dev.namelessnanashi.flipperzero.widget.screen.viewmodel.WidgetSelectViewModel
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

class WidgetOptionsDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val widgetId: Int,
    @Assisted private val navigation: StackNavigation<WidgetNavigationConfig>,
    private val archiveApi: ArchiveApi,
    private val widgetSelectViewModelFactory: WidgetSelectViewModel.Factory
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val widgetSelectViewModel = viewModelWithFactory(key = widgetId.toString()) {
            widgetSelectViewModelFactory(widgetId)
        }
        WidgetOptionsComposable(
            archiveApi = archiveApi,
            widgetSelectViewModel = widgetSelectViewModel,
            onOpenSearchScreen = {
                navigation.pushToFront(
                    WidgetNavigationConfig.SearchScreen {
                        widgetSelectViewModel.onSelectKey(it)
                    }
                )
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            widgetId: Int,
            navigation: StackNavigation<WidgetNavigationConfig>
        ): WidgetOptionsDecomposeComponentImpl
    }
}
