package dev.namelessnanashi.flipperzero.filemanager.upload.impl.deeplink

import dev.namelessnanashi.flipperzero.deeplink.model.DeeplinkContent
import okio.Source

interface DeeplinkContentProvider {
    fun source(deeplinkContent: DeeplinkContent): Source?
}
