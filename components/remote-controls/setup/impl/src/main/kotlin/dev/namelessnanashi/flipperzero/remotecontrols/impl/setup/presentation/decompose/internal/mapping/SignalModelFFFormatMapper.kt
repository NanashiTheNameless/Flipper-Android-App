package dev.namelessnanashi.flipperzero.remotecontrols.impl.setup.presentation.decompose.internal.mapping

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.ifrmvp.backend.model.SignalModel
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_ADDRESS
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_COMMAND
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_DATA
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_DUTY_CYCLE
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FILE_TYPE_KEY
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FILE_TYPE_VALUE
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FILE_VERSION_KEY
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FILE_VERSION_VALUE
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FREQUENCY
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_NAME
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_PROTOCOL
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_TYPE

internal fun SignalModel.toFFFormat(): FlipperFileFormat {
    return FlipperFileFormat(
        orderedDict = listOf(
            (KEY_FILE_TYPE_KEY to KEY_FILE_TYPE_VALUE),
            (KEY_FILE_VERSION_KEY to KEY_FILE_VERSION_VALUE),
            (KEY_NAME to remote.name),
            (KEY_TYPE to remote.type),
            (KEY_FREQUENCY to remote.frequency),
            (KEY_DUTY_CYCLE to remote.dutyCycle),
            (KEY_DATA to remote.data),
            (KEY_PROTOCOL to remote.protocol),
            (KEY_ADDRESS to remote.address),
            (KEY_COMMAND to remote.command),
        ).mapNotNull { (k, v) -> if (v == null) null else k to v }
    )
}
