package dev.namelessnanashi.flipperzero.remotecontrols.grid.remote.api.model

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.UI_INFRARED_EXTENSION

data class ServerRemoteControlParam(
    val infraredFileId: Long,
    val remoteName: String
) {

    val key: String
        get() = this.toString()

    val extTempFolderPath: String
        get() = "${FlipperKeyType.INFRARED.flipperDir}/temp/"

    val nameWithExtension: String
        get() = "$remoteName.ir"

    val uiFileNameWithExtension: String
        get() = "$remoteName.$UI_INFRARED_EXTENSION"
}
