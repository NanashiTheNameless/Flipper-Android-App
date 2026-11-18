package dev.namelessnanashi.flipperzero.rootscreen.impl.deeplink

import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.bringToFront
import com.arkivanov.decompose.router.stack.pushToFront
import com.arkivanov.decompose.value.Value
import dev.namelessnanashi.flipperzero.bottombar.api.BottomBarDecomposeComponent
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.warn
import dev.namelessnanashi.flipperzero.deeplink.model.Deeplink
import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkBottomBarTab
import dev.namelessnanashi.flipperzero.firstpair.api.FirstPairApi
import dev.namelessnanashi.flipperzero.rootscreen.api.RootDeeplinkHandler
import dev.namelessnanashi.flipperzero.rootscreen.model.RootScreenConfig
import dev.namelessnanashi.flipperzero.ui.decompose.DecomposeComponent
import dev.namelessnanashi.flipperzero.ui.decompose.findComponentByConfig

class RootDeeplinkHandler(
    private val navigation: StackNavigation<RootScreenConfig>,
    private val stack: Value<ChildStack<RootScreenConfig, DecomposeComponent>>,
    private val firstPairApi: FirstPairApi
) : RootDeeplinkHandler, LogTagProvider {
    override val TAG = "RootDeeplinkHandler"

    override fun handleDeeplink(deeplink: Deeplink) {
        when (deeplink) {
            is Deeplink.RootLevel -> {
                if (deeplink is Deeplink.RootLevel.SaveKey) {
                    notifyBottomBar(Deeplink.BottomBar.OpenTab(DeeplinkBottomBarTab.ARCHIVE))
                }
                navigation.pushToFront(getConfigFromRootLevelDeeplink(deeplink))
            }

            is Deeplink.BottomBar -> {
                if (firstPairApi.shouldWeOpenPairScreen()) {
                    navigation.bringToFront(RootScreenConfig.FirstPair(deeplink))
                    return
                }
                notifyBottomBar(deeplink)
            }
        }
    }

    private fun notifyBottomBar(deeplink: Deeplink.BottomBar) {
        val component = stack.findComponentByConfig(RootScreenConfig.BottomBar::class)
        if (component == null || component !is BottomBarDecomposeComponent<*>) {
            warn { "Bottom bar component is not exist in stack, but first pair screen already passed" }
            navigation.bringToFront(RootScreenConfig.BottomBar(deeplink))
            return
        }
        component.handleDeeplink(deeplink)
    }

    companion object {
        fun getConfigStackFromDeeplink(deeplink: Deeplink?): List<RootScreenConfig> {
            return when (deeplink) {
                is Deeplink.BottomBar -> if (deeplink is Deeplink.BottomBar.ArchiveTab.ArchiveCategory.OpenKey) {
                    listOf(
                        RootScreenConfig.BottomBar(deeplink),
                        RootScreenConfig.OpenKey(deeplink.keyPath)
                    )
                } else {
                    listOf(RootScreenConfig.BottomBar(deeplink))
                }

                is Deeplink.RootLevel -> listOf(
                    RootScreenConfig.BottomBar(
                        if (deeplink is Deeplink.RootLevel.SaveKey) {
                            Deeplink.BottomBar.OpenTab(DeeplinkBottomBarTab.ARCHIVE)
                        } else {
                            null
                        }
                    ),
                    getConfigFromRootLevelDeeplink(deeplink)
                )

                null -> listOf(RootScreenConfig.BottomBar(null))
            }
        }

        private fun getConfigFromRootLevelDeeplink(deeplink: Deeplink.RootLevel): RootScreenConfig {
            return when (deeplink) {
                is Deeplink.RootLevel.SaveKey -> RootScreenConfig.SaveKey(deeplink)
                is Deeplink.RootLevel.WidgetOptions -> RootScreenConfig.WidgetOptions(deeplink.appWidgetId)
            }
        }
    }
}
