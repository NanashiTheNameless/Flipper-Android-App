package dev.namelessnanashi.flipperzero.keyparser.api

import android.net.Uri
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyCrypto
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed

interface KeyParser {
    suspend fun parseKey(flipperKey: FlipperKey): FlipperKeyParsed

    suspend fun parseUri(uri: Uri): Pair<FlipperFilePath, FlipperFileFormat>?

    suspend fun keyToUrl(flipperKey: FlipperKey): String

    fun cryptoKeyDataToUri(key: FlipperKeyCrypto): String

    fun parseUriToCryptoKeyData(uri: Uri): FlipperKeyCrypto?
}
