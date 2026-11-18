package dev.namelessnanashi.flipperzero.share.receive.helpers

import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.SimpleKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.delegates.key.UtilsKeyApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.inappnotification.api.InAppNotificationStorage
import dev.namelessnanashi.flipperzero.inappnotification.api.model.InAppNotification
import dev.namelessnanashi.flipperzero.keyparser.api.KeyParser
import dev.namelessnanashi.flipperzero.keyparser.api.model.FlipperKeyParsed
import dev.namelessnanashi.flipperzero.share.receive.R
import dev.zacsweers.metro.Inject

class ReceiveKeyActionHelper @Inject constructor(
    private val notificationStorage: InAppNotificationStorage,
    private val simpleKeyApi: SimpleKeyApi,
    private val keyParser: KeyParser,
    private val utilsKeyApi: UtilsKeyApi
) {
    suspend fun saveKey(key: FlipperKey): Result<Unit> = runCatching {
        simpleKeyApi.insertKey(key)
        notificationStorage.addNotification(
            InAppNotification.Successful(
                title = key.path.nameWithoutExtension,
                descId = R.string.saved_key_desc
            )
        )
    }

    private suspend fun findNewPathForKey(flipperKey: FlipperKey): FlipperKeyPath {
        return utilsKeyApi.findAvailablePath(flipperKey.getKeyPath())
    }

    private fun cloneKeyByPath(flipperKey: FlipperKey, newPath: FlipperKeyPath): FlipperKey {
        return flipperKey.copy(
            mainFile = flipperKey.mainFile.copy(
                path = newPath.path
            ),
            deleted = newPath.deleted
        )
    }

    suspend fun findNewPathAndCloneKey(flipperKey: FlipperKey): FlipperKey {
        val newPath = findNewPathForKey(flipperKey)
        return cloneKeyByPath(flipperKey, newPath)
    }

    suspend fun parseKey(flipperKey: FlipperKey): FlipperKeyParsed {
        return keyParser.parseKey(flipperKey)
    }
}
