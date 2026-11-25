package dev.namelessnanashi.flipperzero.bridge.dao.api.model

import dev.namelessnanashi.flipperzero.core.kmpparcelize.KMPIgnoreOnParcel
import dev.namelessnanashi.flipperzero.core.kmpparcelize.KMPParcelable
import dev.namelessnanashi.flipperzero.core.kmpparcelize.KMPParcelize
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import java.io.File

private const val FLIPPER_STORAGE_NAME = "/ext/"

/**
 * Describe name and path for key.
 * Used as unique id for keys
 *
 * Never contains storage
 */
@Serializable
@KMPParcelize
data class FlipperFilePath(
    @SerialName("folder")
    val folder: String,
    @SerialName("name")
    val nameWithExtension: String, // With extension
    // Favorites may point at internal files, directories, or Momentum /app: shortcuts.
    @SerialName("absolute_path")
    val absolutePath: String? = null
) : KMPParcelable, Comparable<FlipperFilePath> {
    @KMPIgnoreOnParcel
    val extension: String
        get() = nameWithExtension.substringAfterLast('.')

    @KMPIgnoreOnParcel
    val pathToKey: String by lazy {
        if (absolutePath != null) return@lazy absolutePath
        var path = File(folder, nameWithExtension).path
        if (path.firstOrNull() == File.separatorChar) {
            path = path.replaceFirst(File.separatorChar.toString(), "")
        }
        return@lazy path
    }

    @KMPIgnoreOnParcel
    val keyType: FlipperKeyType? by lazy {
        FlipperKeyType.getByExtension(extension)
    }

    @KMPIgnoreOnParcel
    val fileType: FlipperFileType by lazy {
        FlipperFileType.getByExtension(extension)
    }

    @KMPIgnoreOnParcel
    val nameWithoutExtension by lazy {
        nameWithExtension.substringAfterLast('/').substringBeforeLast(".")
    }

    fun getPathOnFlipper(): String = absolutePath ?: File(FLIPPER_STORAGE_NAME, pathToKey).path

    override fun toString() = pathToKey

    companion object {
        fun fromFavoritePath(value: String): FlipperFilePath {
            val path = value.trimEnd('\r')
            if (path.startsWith('/') && !path.startsWith(FLIPPER_STORAGE_NAME)) {
                return FlipperFilePath(path.substringBeforeLast('/'), path.substringAfterLast('/'), path)
            }
            val relative = path.removePrefix(FLIPPER_STORAGE_NAME)
            return FlipperFilePath(relative.substringBefore('/', ""), relative.substringAfter('/', relative))
        }

        val DUMMY by lazy {
            FlipperFilePath(
                FlipperKeyType.NFC.flipperDir,
                "Test_Key.nfc"
            )
        }
    }

    fun copyWithChangedName(newNameWithoutExtension: String): FlipperFilePath {
        return FlipperFilePath(folder, "$newNameWithoutExtension.$extension")
    }

    override fun equals(other: Any?): Boolean = other is FlipperFilePath && pathToKey == other.pathToKey

    override fun hashCode(): Int = pathToKey.hashCode()

    override fun compareTo(other: FlipperFilePath): Int {
        return pathToKey.compareTo(other.pathToKey)
    }
}
