package dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.shared

import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.BoxWithConstraintsScope
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.button.ButtonItemComposable
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.button.core.ButtonClickEvent
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.core.GridItemComposable
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.core.LocalScaleFactor
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.core.rememberScaleFactor
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.core.sf
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.util.GridConstants
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrButton
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.ifrmvp.model.PageLayout
import dev.namelessnanashi.flipperzero.remotecontrols.coreui.R as RemoteControlsR

@Composable
internal fun BoxWithConstraintsScope.ButtonsComposable(
    pageLayout: PageLayout?,
    emulatedKeyIdentifier: IfrKeyIdentifier?,
    isSyncing: Boolean,
    isConnected: Boolean,
    onButtonClick: (IfrButton, ButtonClickEvent, IfrKeyIdentifier) -> Unit,
    modifier: Modifier = Modifier,
    onReload: (() -> Unit)? = null,
) {
    if (pageLayout?.buttons.isNullOrEmpty()) {
        ErrorComposable(
            desc = stringResource(RemoteControlsR.string.empty_page),
            onReload = onReload
        )
    }
    BoxWithConstraints(
        modifier = modifier
            .align(Alignment.Center)
            .fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        val scaleFactor = rememberScaleFactor()
        CompositionLocalProvider(LocalScaleFactor provides scaleFactor) {
            BoxWithConstraints(
                modifier = Modifier
                    .width(GridConstants.SCALE_WIDTH.sf)
                    .height(GridConstants.SCALE_HEIGHT.sf),
            ) {
                pageLayout?.buttons
                    .orEmpty()
                    .forEach { button ->
                        GridItemComposable(
                            modifier = Modifier,
                            position = button.position,
                            content = {
                                ButtonItemComposable(
                                    buttonData = button.data,
                                    emulatedKeyIdentifier = emulatedKeyIdentifier,
                                    isSyncing = isSyncing,
                                    isConnected = isConnected,
                                    onKeyDataClick = { clickType, keyIdentifier ->
                                        onButtonClick.invoke(button, clickType, keyIdentifier)
                                    }
                                )
                            }
                        )
                    }
            }
        }
    }
}
