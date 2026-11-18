package dev.namelessnanashi.flipperzero.bridge.dao.impl.converters

import androidx.room.TypeConverter
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType

class FlipperFileTypeConverter {
    @TypeConverter
    fun extensionToFileType(extension: String?): FlipperKeyType? {
        val extensionNotNull = extension ?: return null
        return FlipperKeyType.getByExtension(extensionNotNull)
    }

    @TypeConverter
    fun fileTypeToExtension(fileType: FlipperKeyType?): String? {
        return fileType?.extension
    }
}
