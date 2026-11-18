package dev.namelessnanashi.flipperzero.main.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.pushToFront
import dev.namelessnanashi.flipperzero.core.ui.lifecycle.viewModelWithFactory
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.faphub.catalogtab.api.CatalogTabApi
import dev.namelessnanashi.flipperzero.faphub.installedtab.api.FapInstalledApi
import dev.namelessnanashi.flipperzero.main.impl.composable.ComposableFapHubMainScreen
import dev.namelessnanashi.flipperzero.main.impl.model.FapHubNavigationConfig
import dev.namelessnanashi.flipperzero.main.impl.viewmodel.MainViewModel
import dev.namelessnanashi.flipperzero.ui.decompose.ScreenDecomposeComponent
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject

@Suppress("LongParameterList")
class MainScreenDecomposeComponentImpl @AssistedInject constructor(
    @Assisted componentContext: ComponentContext,
    @Assisted private val navigation: StackNavigation<FapHubNavigationConfig>,
    @Assisted private val deeplink: Deeplink.BottomBar.AppsTab.MainScreen?,
    private val catalogTabApi: CatalogTabApi,
    private val installedApi: FapInstalledApi,
    private val mainViewModelFactory: MainViewModel.Factory
) : ScreenDecomposeComponent(componentContext) {

    @Composable
    @Suppress("NonSkippableComposable")
    override fun Render() {
        val mainViewModel = viewModelWithFactory(key = deeplink) {
            mainViewModelFactory(deeplink)
        }
        val selectedTab by mainViewModel.getTabFlow().collectAsState()

        val readyToUpdateCount = installedApi.getUpdatePendingCount(this)

        ComposableFapHubMainScreen(
            catalogTabComposable = {
                catalogTabApi.ComposableCatalogTab(
                    onOpenFapItem = {
                        navigation.pushToFront(FapHubNavigationConfig.FapScreen(it.id))
                    },
                    onCategoryClick = {
                        navigation.pushToFront(FapHubNavigationConfig.Category(it))
                    },
                    componentContext = this
                )
            },
            installedTabComposable = {
                installedApi.ComposableInstalledTab(
                    onOpenFapItem = {
                        navigation.pushToFront(FapHubNavigationConfig.FapScreen(it))
                    },
                    componentContext = this
                )
            },
            onOpenSearch = {
                navigation.pushToFront(FapHubNavigationConfig.Search)
            },
            installedNotificationCount = readyToUpdateCount,
            selectedTab = selectedTab,
            onSelect = mainViewModel::onSelectTab
        )
    }

    @AssistedFactory
    fun interface Factory {
        operator fun invoke(
            componentContext: ComponentContext,
            navigation: StackNavigation<FapHubNavigationConfig>,
            deeplink: Deeplink.BottomBar.AppsTab.MainScreen?
        ): MainScreenDecomposeComponentImpl
    }
}
