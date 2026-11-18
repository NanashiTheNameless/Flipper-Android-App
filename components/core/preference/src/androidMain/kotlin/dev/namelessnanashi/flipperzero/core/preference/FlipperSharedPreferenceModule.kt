package dev.namelessnanashi.flipperzero.core.preference

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.dataStore
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.preference.internal.NewPairSettingsSerializer
import dev.namelessnanashi.flipperzero.core.preference.internal.PairSettingsSerializer
import dev.namelessnanashi.flipperzero.core.preference.internal.SettingsSerializer
import dev.namelessnanashi.flipperzero.core.preference.pb.NewPairSettings
import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppGraph::class)
interface FlipperSharedPreferenceModule {
    @Provides
    @SingleIn(AppGraph::class)
    fun provideDataStoreSettings(context: Context): DataStore<Settings> {
        return context.dataStoreSettings
    }

    @Provides
    @SingleIn(AppGraph::class)
    fun provideDataStorePairSettings(
        context: Context
    ): DataStore<PairSettings> {
        return context.dataStorePairSettings
    }

    @Provides
    @SingleIn(AppGraph::class)
    fun provideDataStoreNewPairSetting(
        context: Context
    ): DataStore<NewPairSettings> {
        return context.dataStoreNewPairSettings
    }
}

private val Context.dataStoreNewPairSettings: DataStore<NewPairSettings> by dataStore(
    fileName = SettingsFilePaths.DATASTORE_FILENAME_PAIR_SETTINGS_V2,
    serializer = NewPairSettingsSerializer
)

private val Context.dataStoreSettings: DataStore<Settings> by dataStore(
    fileName = SettingsFilePaths.DATASTORE_FILENAME_SETTINGS,
    serializer = SettingsSerializer
)

private val Context.dataStorePairSettings: DataStore<PairSettings> by dataStore(
    fileName = SettingsFilePaths.DATASTORE_FILENAME_PAIR_SETTINGS,
    serializer = PairSettingsSerializer
)
