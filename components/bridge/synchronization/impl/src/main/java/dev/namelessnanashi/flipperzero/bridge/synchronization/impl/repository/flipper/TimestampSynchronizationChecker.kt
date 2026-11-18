package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.repository.flipper

import dev.namelessnanashi.flipperzero.bridge.connection.feature.storage.api.fm.FFileTimestampApi
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.pmap
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import dev.zacsweers.metro.ContributesBinding
import java.util.concurrent.atomic.AtomicLong
import dev.zacsweers.metro.Inject
import kotlin.io.path.Path
import dev.zacsweers.metro.binding

interface TimestampSynchronizationChecker {
    data object TimestampsProgressDetail : DetailedProgressListener.Detail

    suspend fun fetchFoldersTimestamp(
        types: Array<FlipperKeyType>,
        progressTracker: DetailedProgressWrapperTracker
    ): Map<FlipperKeyType, Long?>
}

@ContributesBinding(TaskGraph::class, binding<TimestampSynchronizationChecker>())
class TimestampSynchronizationCheckerImpl @Inject constructor(
    private val timestampApi: FFileTimestampApi,
) : TimestampSynchronizationChecker, LogTagProvider {
    override val TAG = "TimestampSynchronizationChecker"

    override suspend fun fetchFoldersTimestamp(
        types: Array<FlipperKeyType>,
        progressTracker: DetailedProgressWrapperTracker
    ): Map<FlipperKeyType, Long?> {
        val resultCounter = AtomicLong(0)

        progressTracker.report(
            current = resultCounter.get(),
            max = types.size.toLong(),
            detail = TimestampSynchronizationChecker.TimestampsProgressDetail
        )

        val timestampHashes = types.toList().pmap { type ->
            val response = timestampApi.fetchFolderTimestamp(
                folder = Path("/ext/").resolve(type.flipperDir).toString()
            )
            progressTracker.report(
                current = resultCounter.incrementAndGet(),
                max = types.size.toLong(),
                detail = TimestampSynchronizationChecker.TimestampsProgressDetail
            )
            type to response
        }.associate { (type, timestampOrNull) ->
            type to timestampOrNull
        }
        info { "Timestamp hashes is $timestampHashes" }

        return timestampHashes
    }
}
