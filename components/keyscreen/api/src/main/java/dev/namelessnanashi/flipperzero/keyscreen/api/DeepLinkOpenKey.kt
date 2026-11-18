package dev.namelessnanashi.flipperzero.keyscreen.api

import android.content.Intent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath

interface DeepLinkOpenKey {
    fun getIntentForOpenKey(keyPath: FlipperKeyPath): Intent
}
