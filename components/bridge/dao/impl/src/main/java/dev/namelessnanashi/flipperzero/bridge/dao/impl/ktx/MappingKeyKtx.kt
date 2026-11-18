package dev.namelessnanashi.flipperzero.bridge.dao.impl.ktx

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFile
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKey
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyPath
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.DatabaseKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.Key
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.SynchronizedStatus
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.AdditionalFileDao
import kotlinx.collections.immutable.toImmutableList

internal fun FlipperKey.toDatabaseKey(): Key {
    return Key(
        path = path.pathToKey,
        type = path.keyType,
        content = DatabaseKeyContent(keyContent),
        notes = notes,
        synchronizedStatus = if (synchronized) {
            SynchronizedStatus.SYNCHRONIZED
        } else {
            SynchronizedStatus.NOT_SYNCHRONIZED
        },
        deleted = deleted
    )
}

internal suspend fun Key.toFlipperKey(additionalFileDao: AdditionalFileDao): FlipperKey {
    return FlipperKey(
        mainFile = FlipperFile(
            path = mainFilePath,
            content = content.flipperContent
        ),
        notes = notes,
        synchronized = synchronizedStatus == SynchronizedStatus.SYNCHRONIZED,
        deleted = deleted,
        additionalFiles = additionalFileDao.getFilesForKeyWithId(uid)
            .map {
                it.toFlipperFile()
            }.toImmutableList()
    )
}

internal fun Key.getFlipperKeyPath(): FlipperKeyPath {
    return FlipperKeyPath(
        path = mainFilePath,
        deleted = deleted
    )
}
