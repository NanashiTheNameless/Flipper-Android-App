package dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.DatabaseKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.FlipperAdditionalFile

internal fun FlipperAdditionalFile.toFlipperFile(): FlipperFile = FlipperFile(
    path = filePath,
    content = content.flipperContent
)

internal fun FlipperFile.toDatabaseFile(keyId: Int) = FlipperAdditionalFile(
    path = path.pathToKey,
    content = DatabaseKeyContent(content),
    keyId = keyId
)
