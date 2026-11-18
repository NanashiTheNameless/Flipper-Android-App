package dev.namelessnanashi.flipperzero.ifrmvp.core.ui.preview

import android.content.res.Configuration
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.ifrmvp.core.ui.layout.shared.GridPagesContent
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrButton
import dev.namelessnanashi.flipperzero.ifrmvp.model.IfrKeyIdentifier
import dev.namelessnanashi.flipperzero.ifrmvp.model.PageLayout
import dev.namelessnanashi.flipperzero.ifrmvp.model.PagesLayout
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.IconButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.TextButtonData
import dev.namelessnanashi.flipperzero.ifrmvp.model.buttondata.VolumeButtonData

@Preview(
    showSystemUi = true,
    showBackground = true,
    uiMode = Configuration.UI_MODE_NIGHT_YES
)
@Composable
@Suppress("LongMethod")
private fun LoadedContentPreview() {
    FlipperThemeInternal {
        GridPagesContent(
            pagesLayout = PagesLayout(
                pages = listOf(
                    PageLayout(
                        buttons = listOf(
                            IfrButton(
                                data = IconButtonData(
                                    keyIdentifier = IfrKeyIdentifier.Unknown,
                                    iconId = IconButtonData.IconType.POWER
                                ),
                                position = IfrButton.Position(
                                    x = 1,
                                    y = 0,
                                    zIndex = 10f,
                                    alignment = IfrButton.Alignment.CENTER,
                                ),
                            ),
                            IfrButton(
                                data = IconButtonData(
                                    keyIdentifier = IfrKeyIdentifier.Unknown,
                                    iconId = IconButtonData.IconType.POWER
                                ),
                                position = IfrButton.Position(
                                    x = 2,
                                    y = 0,
                                    alignment = IfrButton.Alignment.CENTER,
                                ),
                            ),
                            IfrButton(
                                data = TextButtonData(
                                    keyIdentifier = IfrKeyIdentifier.Unknown,
                                    text = "PWR"
                                ),
                                position = IfrButton.Position(0, 0)
                            ),
                            IfrButton(
                                data = TextButtonData(
                                    keyIdentifier = IfrKeyIdentifier.Unknown,
                                    text = "PWR"
                                ),
                                position = IfrButton.Position(
                                    y = 1,
                                    x = 0,
                                )
                            ),
                            IfrButton(
                                data = VolumeButtonData(
                                    addKeyIdentifier = IfrKeyIdentifier.Unknown,
                                    reduceKeyIdentifier = IfrKeyIdentifier.Unknown
                                ),
                                position = IfrButton.Position(
                                    y = 3,
                                    x = 0,
                                    containerHeight = 3
                                )
                            ),
                        )
                    )
                )
            ),
            onButtonClick = { _, _, _ -> },
            onReload = {},
            emulatedKeyIdentifier = null,
            isSyncing = false,
            isConnected = true
        )
    }
}
