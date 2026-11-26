package dev.namelessnanashi.flipperzero.faphub.installation.queue.api.model

import dev.namelessnanashi.flipperzero.core.data.SemVer
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import java.io.File
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.security.MessageDigest

data class FapArtifact(val name: String, val target: Int, val api: SemVer, val version: Long) {
    fun requireCompatible(device: FlipperTarget.Received) {
        require(device.target == "f$target") { "App targets f$target; connected device is ${device.target}" }
        require(api.majorVersion == device.sdk.majorVersion && api.minorVersion <= device.sdk.minorVersion) {
            "App requires API $api; connected firmware provides ${device.sdk}. Rebuild for this firmware."
        }
    }

    companion object {
        const val MAX_BYTES = 32L * 1024 * 1024

        /** Parse bounded ELF32/ARM section tables, without executing or loading the application. */
        fun inspect(file: File): FapArtifact {
            require(file.length() in 52..MAX_BYTES) { "Empty, truncated, or oversized FAP" }
            val bytes = file.readBytes()
            val elf = ByteBuffer.wrap(bytes).order(ByteOrder.LITTLE_ENDIAN)
            fun u16(offset: Int) = elf.getShort(offset).toInt() and 0xffff
            fun u32(offset: Int) = elf.getInt(offset).toLong() and 0xffffffffL
            fun range(offset: Long, length: Long): Int {
                require(offset >= 0 && length >= 0 && offset + length <= bytes.size.toLong()) { "Invalid ELF section bounds" }
                return offset.toInt()
            }
            require(bytes.take(7) == listOf<Byte>(0x7f, 0x45, 0x4c, 0x46, 1, 1, 1)) { "Not a little-endian ELF32 FAP" }
            require(u16(18) == 40 && u16(16) in 1..2) { "Not an ARM application" }
            val table = u32(32)
            val stride = u16(46)
            val count = u16(48)
            val namesIndex = u16(50)
            require(stride >= 40 && count in 1..4096 && namesIndex in 1 until count) { "Invalid ELF section table" }
            range(table, stride.toLong() * count)
            fun section(index: Int) = (table + stride.toLong() * index).toInt()
            val names = section(namesIndex)
            val namesOffset = u32(names + 16)
            val namesSize = u32(names + 20)
            require(namesSize in 1..(1024 * 1024)) { "Invalid ELF section names" }
            range(namesOffset, namesSize)
            val metadata = (1 until count).singleOrNull { index ->
                val name = u32(section(index))
                require(name < namesSize) { "Invalid ELF section name" }
                val start = (namesOffset + name).toInt()
                var end = start
                while (end < namesOffset + namesSize && bytes[end] != 0.toByte()) end++
                require(end < namesOffset + namesSize) { "Unterminated ELF section name" }
                String(bytes, start, end - start, Charsets.US_ASCII) == ".fapmeta"
            } ?: error("Missing or duplicate .fapmeta section")
            val header = section(metadata)
            val offset = range(u32(header + 16), u32(header + 20))
            require(u32(header + 20) >= 85) { "Truncated FAP metadata" }
            require(u32(offset) == 0x52474448L && u32(offset + 4) == 1L) { "Unsupported FAP manifest" }
            val nameBytes = bytes.copyOfRange(offset + 20, offset + 52)
            val terminator = nameBytes.indexOf(0)
            require(terminator >= 0) { "Invalid FAP app name" }
            val name = String(nameBytes, 0, terminator, Charsets.UTF_8)
            require(name.isNotBlank() && name.none { it.isISOControl() }) { "Invalid FAP app name" }
            return FapArtifact(name, u16(offset + 12), SemVer(u16(offset + 10), u16(offset + 8)), u32(offset + 16))
        }

        fun sha256(file: File): String {
            val digest = MessageDigest.getInstance("SHA-256")
            file.inputStream().use { input ->
                val buffer = ByteArray(8192)
                while (true) {
                    val count = input.read(buffer)
                    if (count < 0) break
                    digest.update(buffer, 0, count)
                }
            }
            return digest.digest().joinToString("") { "%02x".format(it) }
        }

        fun requireSegment(value: String) {
            require(value.length in 1..80 && value !in listOf(".", "..") &&
                value.none { it == '/' || it == '\\' || it.isISOControl() }) { "Invalid app filename or category" }
        }
    }
}
