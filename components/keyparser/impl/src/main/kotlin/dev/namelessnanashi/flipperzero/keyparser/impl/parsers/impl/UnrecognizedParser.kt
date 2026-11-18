package dev.namelessnanashi.flipperzero.keyparser.impl.parsers.impl

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyparser.impl.parsers.KeyParserDelegate
import kotlinx.collections.immutable.toImmutableList

class UnrecognizedParser : KeyParserDelegate {
    override suspend fun parseKey(
        flipperKey: FlipperKey,
        fff: FlipperFileFormat
    ): FlipperKeyParsed {
        return FlipperKeyParsed.Unrecognized(
            flipperKey.path.nameWithoutExtension,
            flipperKey.notes,
            flipperKey.path.keyType,
            fff.orderedDict.toImmutableList()
        )
    }
}
