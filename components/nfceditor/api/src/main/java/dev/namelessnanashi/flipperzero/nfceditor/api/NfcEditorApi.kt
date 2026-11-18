package dev.namelessnanashi.flipperzero.nfceditor.api

import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed

interface NfcEditorApi {
    fun isSupportedByNfcEditor(parsedKey: FlipperKeyParsed): Boolean
}
