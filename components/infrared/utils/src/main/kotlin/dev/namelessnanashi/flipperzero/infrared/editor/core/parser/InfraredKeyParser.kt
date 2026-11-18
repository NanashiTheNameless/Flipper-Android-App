package dev.namelessnanashi.flipperzero.infrared.editor.core.parser

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileFormat
import dev.namelessnanashi.flipperzero.infrared.editor.core.model.InfraredRemote
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_ADDRESS
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_COMMAND
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_DATA
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_DUTY_CYCLE
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_FREQUENCY
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_NAME
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_PROTOCOL
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_TYPE
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_TYPE_PARSED
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.KEY_TYPE_RAW
import dev.namelessnanashi.flipperzero.infrared.editor.core.util.InfraredFileFormat.allFields

object InfraredKeyParser {
    fun mapParsedKeyToInfraredRemotes(
        fff: FlipperFileFormat
    ): List<InfraredRemote> {
        val blocks = parseKeyContent(fff.orderedDict)
        return parseRemotes(blocks)
    }

    private fun parseKeyContent(
        input: List<Pair<String, String>>
    ): List<List<Pair<String, String>>> {
        val result = mutableListOf<List<Pair<String, String>>>()
        val currentSection = mutableListOf<Pair<String, String>>()

        for (pair in input) {
            if (pair.first.startsWith(KEY_NAME)) {
                if (currentSection.isNotEmpty()) {
                    result.add(currentSection.toList())
                    currentSection.clear()
                }
            }
            if (allFields.contains(pair.first)) {
                currentSection.add(pair)
            }
        }

        if (currentSection.isNotEmpty()) {
            result.add(currentSection.toList())
        }

        return result
    }

    private fun parseRemotes(
        blocks: List<List<Pair<String, String>>>
    ): List<InfraredRemote> {
        return blocks.mapNotNull { block ->
            val blockMap = block.toMap()
            val type = blockMap[KEY_TYPE]

            when (type) {
                KEY_TYPE_RAW -> parseRemoteRaw(blockMap)
                KEY_TYPE_PARSED -> parseRemoteParsed(blockMap)
                else -> null
            }
        }
    }

    private fun parseRemoteRaw(block: Map<String, String>): InfraredRemote.Raw {
        val name = block[KEY_NAME].orEmpty()
        val type = block[KEY_TYPE].orEmpty()
        val frequency = block[KEY_FREQUENCY].orEmpty()
        val dutyCycle = block[KEY_DUTY_CYCLE].orEmpty()
        val data = block[KEY_DATA].orEmpty()
        return InfraredRemote.Raw(name, type, frequency, dutyCycle, data)
    }

    private fun parseRemoteParsed(block: Map<String, String>): InfraredRemote.Parsed {
        val name = block[KEY_NAME].orEmpty()
        val type = block[KEY_TYPE].orEmpty()
        val protocol = block[KEY_PROTOCOL].orEmpty()
        val address = block[KEY_ADDRESS].orEmpty()
        val command = block[KEY_COMMAND].orEmpty()
        return InfraredRemote.Parsed(name, type, protocol, address, command)
    }
}
