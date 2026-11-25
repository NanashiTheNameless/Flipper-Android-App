package dev.namelessnanashi.flipperzero.core.preference.internal

import androidx.datastore.core.DataMigration
import com.squareup.wire.ProtoAdapter
import com.squareup.wire.ProtoReader
import dev.namelessnanashi.flipperzero.core.preference.pb.PairSettings
import okio.Buffer

/** Remove the retired field from existing installs without resetting device setup. */
internal object RemovePolicyAcceptanceMigration : DataMigration<PairSettings> {
    override suspend fun shouldMigrate(currentData: PairSettings) =
        currentData != migrate(currentData)

    override suspend fun migrate(currentData: PairSettings): PairSettings {
        if (currentData.unknownFields.size == 0) return currentData
        val reader = ProtoReader(Buffer().write(currentData.unknownFields))
        val remaining = reader.forEachTag { tag ->
            if (tag == 1) ProtoAdapter.BOOL.decode(reader) else reader.readUnknownField(tag)
        }
        return currentData.copy(unknownFields = remaining)
    }

    override suspend fun cleanUp() = Unit
}
