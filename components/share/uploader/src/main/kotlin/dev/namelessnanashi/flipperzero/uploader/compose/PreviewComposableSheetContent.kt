package dev.namelessnanashi.flipperzero.uploader.compose

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.core.ui.theme.FlipperThemeInternal
import dev.namelessnanashi.flipperzero.uploader.models.ShareContent
import dev.namelessnanashi.flipperzero.uploader.models.ShareError
import dev.namelessnanashi.flipperzero.uploader.models.ShareState

@Preview
@Composable
private fun ComposableSheetContentErrorOtherPreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.Error(ShareError.OTHER),
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ComposableSheetContentErrorInternetPreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.Error(ShareError.NO_INTERNET_CONNECTION),
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ComposableSheetContentErrorServerPreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.Error(ShareError.CANT_CONNECT_TO_SERVER),
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ComposableSheetContentInitPreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.Initial,
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ComposableSheetContentPreparePreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.Prepare,
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}

@Preview
@Composable
private fun ComposableSheetContentPendingPreview() {
    FlipperThemeInternal {
        ComposableSheetContent(
            state = ShareState.PendingShare(
                content = ShareContent(
                    link = "https://flipperdevices.com",
                    flipperKey = FlipperKey(
                        mainFile = FlipperFile(
                            path = FlipperFilePath(
                                folder = "sub",
                                nameWithExtension = "test.sub"
                            ),
                            content = FlipperKeyContent.RawData(byteArrayOf())
                        ),
                        synchronized = true,
                        deleted = false
                    )
                )
            ),
            keyName = "keyName",
            onShareLink = {},
            onShareFile = {},
            onRetry = {},
            onClose = {}
        )
    }
}
