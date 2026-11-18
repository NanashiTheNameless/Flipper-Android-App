package dev.namelessnanashi.flipperzero.nfceditor.impl.api

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.nfceditor.api.NfcEditorApi
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject

@Suppress("MagicNumber")
private val SUPPORTED_NFC_FORMATS = intArrayOf(2, 3, 4)
private const val SUPPORTED_NFC_TYPE = "Mifare Classic"

@ContributesBinding(AppGraph::class)
class NfcEditorApiImpl @Inject constructor() : NfcEditorApi {
    override fun isSupportedByNfcEditor(parsedKey: FlipperKeyParsed): Boolean {
        return parsedKey is FlipperKeyParsed.NFC &&
            SUPPORTED_NFC_FORMATS.contains(parsedKey.version) &&
            parsedKey.deviceType == SUPPORTED_NFC_TYPE
    }
}
