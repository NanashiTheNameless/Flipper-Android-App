package dev.namelessnanashi.flipperzero.unhandledexception.impl.api

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.di.provideDelegate
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.rootscreen.api.LocalDeeplinkHandler
import dev.namelessnanashi.flipperzero.unhandledexception.api.UnhandledExceptionRenderApi
import dev.namelessnanashi.flipperzero.unhandledexception.impl.composable.ComposableUnhandledExceptionDialog
import dev.zacsweers.metro.ContributesBinding
import kotlinx.coroutines.runBlocking
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<UnhandledExceptionRenderApi>())
class UnhandledExceptionRenderApiImpl @Inject constructor(
    dataStoreProvider: Provider<DataStore<Settings>>,
) : UnhandledExceptionRenderApi {

    val dataStore by dataStoreProvider

    @Composable
    override fun ComposableUnhandledExceptionRender(modifier: Modifier) {
        val settingData by dataStore.data.collectAsState(
            initial = null
        )

        if (settingData?.fatal_ble_security_exception_happens == true) {
            val deeplinkHandler = LocalDeeplinkHandler.current
            ComposableUnhandledExceptionDialog(
                modifier = modifier,
                onDismiss = {
                    runBlocking {
                        dataStore.updateData {
                            it.copy(
                                fatal_ble_security_exception_happens = false
                            )
                        }
                    }
                    deeplinkHandler.handleDeeplink(Deeplink.BottomBar.OpenTab(DeeplinkBottomBarTab.DEVICE))
                }
            )
        }
    }
}
