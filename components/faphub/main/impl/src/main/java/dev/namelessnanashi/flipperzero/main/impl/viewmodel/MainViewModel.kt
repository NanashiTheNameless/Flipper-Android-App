package dev.namelessnanashi.flipperzero.main.impl.viewmodel

import dev.namelessnanashi.flipperzero.core.ui.lifecycle.DecomposeViewModel
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.main.impl.model.FapHubTabEnum
import dev.zacsweers.metro.Assisted
import dev.zacsweers.metro.AssistedFactory
import dev.zacsweers.metro.AssistedInject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class MainViewModel @AssistedInject constructor(
    @Assisted deeplink: Deeplink.BottomBar.AppsTab.MainScreen?
) : DecomposeViewModel() {
    private val tabFlow = MutableStateFlow(
        when (deeplink) {
            Deeplink.BottomBar.AppsTab.MainScreen.InstalledTab -> FapHubTabEnum.INSTALLED
            null -> FapHubTabEnum.APPS
        }
    )

    fun getTabFlow(): StateFlow<FapHubTabEnum> = tabFlow

    fun onSelectTab(tabEnum: FapHubTabEnum) {
        viewModelScope.launch {
            tabFlow.emit(tabEnum)
        }
    }

    @AssistedFactory
    interface Factory {
        operator fun invoke(
            deeplink: Deeplink.BottomBar.AppsTab.MainScreen?
        ): MainViewModel
    }
}
