package dev.namelessnanashi.flipperzero.uploader.models

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey

data class ShareContent(
    val link: String?,
    val flipperKey: FlipperKey
)
