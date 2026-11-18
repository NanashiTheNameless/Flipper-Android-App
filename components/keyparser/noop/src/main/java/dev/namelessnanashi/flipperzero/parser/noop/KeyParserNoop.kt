package dev.namelessnanashi.flipperzero.parser.noop

import android.net.Uri
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyCrypto
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding

@ContributesBinding(AppGraph::class, binding<KeyParser>())
class KeyParserNoop @Inject constructor() : KeyParser {
    override suspend fun parseKey(flipperKey: FlipperKey): FlipperKeyParsed {
        throw NotImplementedError()
    }

    override suspend fun parseUri(uri: Uri): Pair<FlipperFilePath, FlipperFileFormat>? {
        throw NotImplementedError()
    }

    override suspend fun keyToUrl(flipperKey: FlipperKey): String {
        throw NotImplementedError()
    }

    override fun cryptoKeyDataToUri(key: FlipperKeyCrypto): String {
        throw NotImplementedError()
    }

    override fun parseUriToCryptoKeyData(uri: Uri): FlipperKeyCrypto? {
        throw NotImplementedError()
    }
}
