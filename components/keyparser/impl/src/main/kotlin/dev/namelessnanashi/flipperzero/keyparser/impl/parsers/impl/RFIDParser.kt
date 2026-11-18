package dev.namelessnanashi.flipperzero.keyparser.impl.parsers.impl

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.keyparser.impl.parsers.KeyParserDelegate

private const val KEY_TYPE = "Key type"
private const val KEY_DATA = "Data"

class RFIDParser : KeyParserDelegate {
    override suspend fun parseKey(
        flipperKey: FlipperKey,
        fff: FlipperFileFormat
    ): FlipperKeyParsed {
        val keyContentAsMap = fff.orderedDict.toMap()

        return FlipperKeyParsed.RFID(
            keyName = flipperKey.path.nameWithoutExtension,
            notes = flipperKey.notes,
            data = keyContentAsMap[KEY_DATA],
            keyType = keyContentAsMap[KEY_TYPE]
        )
    }
}
