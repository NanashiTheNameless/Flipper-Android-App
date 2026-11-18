package dev.namelessnanashi.flipperzero.bridge.dao.impl

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import dev.namelessnanashi.flipperzero.bridge.dao.impl.converters.DatabaseKeyContentConverter
import dev.namelessnanashi.flipperzero.bridge.dao.impl.converters.FlipperFileTypeConverter
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.FavoriteKey
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.FlipperAdditionalFile
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.HideFapHubApp
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.Key
import dev.namelessnanashi.flipperzero.bridge.dao.impl.model.WidgetDataElement
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.AdditionalFileDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.FavoriteDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.HideFapHubAppDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.KeyDao
import dev.namelessnanashi.flipperzero.bridge.dao.impl.repository.WidgetDataDao

@Database(
    entities = [
        Key::class,
        FavoriteKey::class,
        FlipperAdditionalFile::class,
        WidgetDataElement::class,
        HideFapHubApp::class
    ],
    autoMigrations = [
        AutoMigration(
            from = 1,
            to = 2
        ),
        // From 2 to 4 we just drop database
        AutoMigration(
            from = 4,
            to = 5
        ),
        // From 5 to 6 we just drop database
        AutoMigration(
            from = 6,
            to = 7
        ),
        AutoMigration(
            from = 7,
            to = 8
        ),
        AutoMigration(
            from = 8,
            to = 9
        ),
        AutoMigration(
            from = 9,
            to = 10
        )
    ],
    version = 10,
    exportSchema = true
)
@TypeConverters(
    FlipperFileTypeConverter::class,
    DatabaseKeyContentConverter::class
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun keyDao(): KeyDao
    abstract fun favoriteDao(): FavoriteDao
    abstract fun additionalFileDao(): AdditionalFileDao
    abstract fun widgetDataDao(): WidgetDataDao
    abstract fun fapHubHideApp(): HideFapHubAppDao
}
