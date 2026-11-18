package dev.namelessnanashi.flipperzero.toolstab.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.toolstab.impl.composable.ComposableHub
import dev.namelessnanashi.flipperzero.toolstab.impl.model.ToolsNavigationConfig
import dev.namelessnanashi.flipperzero.toolstab.impl.viewmodel.ToolsNotificationViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import dev.zacsweers.metro.Provider

class ToolsMainScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val navigation: StackNavigation<ToolsNavigationConfig>,
    private val toolsNotificationViewModelProvider: Provider<ToolsNotificationViewModel>,
) : ScreenDecomposeComponent(componentContext) {
    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val nfcAttackViewModel = viewModelWithFactory(key = null) {
            toolsNotificationViewModelProvider.invoke()
        }
        val hasNotification by nfcAttackViewModel.hasNotificationStateFlow.collectAsState()

        ComposableHub(
            hasNotification = hasNotification,
            onOpenMfKey32 = {
                navigation.pushToFront(ToolsNavigationConfig.MfKey32)
            },
            onOpenRemoteControls = {
                navigation.pushToFront(ToolsNavigationConfig.RemoteControls)
            }
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            navigation: StackNavigation<ToolsNavigationConfig>
        ): ToolsMainScreenDecomposeComponentImpl
    }
}
