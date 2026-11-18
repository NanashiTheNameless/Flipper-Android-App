package dev.namelessnanashi.flipperzero.bridge.synchronization.impl.executor

import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFilePath
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyContent
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperFileType
import dev.namelessnanashi.flipperzero.bridge.dao.api.model.FlipperKeyType
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.di.TaskGraph
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyAction
import dev.namelessnanashi.flipperzero.bridge.synchronization.impl.model.KeyDiff
import dev.namelessnanashi.flipperzero.core.ktx.jre.FlipperDispatchers
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressListener
import dev.namelessnanashi.flipperzero.core.progress.DetailedProgressWrapperTracker
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.async
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.ensureActive
import kotlinx.coroutines.sync.Semaphore
import kotlinx.coroutines.sync.withPermit

private const val PARALLEL_LOADS = 4

interface DiffKeyExecutor {
    data class DiffProgressDetail(
        val fileName: String
    ) : DetailedProgressListener.Detail

    suspend fun executeBatch(
        source: AbstractKeyStorage,
        target: AbstractKeyStorage,
        diffs: List<KeyDiff>,
        tracker: DetailedProgressWrapperTracker
    ): List<KeyDiff>
}

/**
 * This class execute diff for
 */
@ContributesBinding(TaskGraph::class, binding<DiffKeyExecutor>())
class DiffKeyExecutorImpl @Inject constructor() : DiffKeyExecutor, LogTagProvider {
    override val TAG = "DiffKeyExecutor"

    /**
     * @return list of diffs which successful applied
     */
    override suspend fun executeBatch(
        source: AbstractKeyStorage,
        target: AbstractKeyStorage,
        diffs: List<KeyDiff>,
        tracker: DetailedProgressWrapperTracker
    ): List<KeyDiff> = coroutineScope {
        val sortedDiffs = diffs.sortedWith(
            compareBy(
                { it.newHash.keyPath.fileType },
                { it.action }
            )
        )
        // Reading one file is a full request/response round trip, so loading files one by one
        // leaves the link idle most of the time. Keep a few loads in flight; writes stay in order.
        val loadPermits = Semaphore(PARALLEL_LOADS)
        val loadedContents = sortedDiffs.map { diff ->
            if (diff.action == KeyAction.DELETED) {
                null
            } else {
                async(FlipperDispatchers.workStealingDispatcher) {
                    loadPermits.withPermit {
                        runCatching { source.loadFile(diff.newHash.keyPath) }
                    }
                }
            }
        }

        sortedDiffs.mapIndexedNotNull { index, diff ->
            try {
                info { "Execute $diff for $source to $target" }

                tracker.report(
                    index.toLong(),
                    diffs.size.toLong(),
                    DiffKeyExecutor.DiffProgressDetail(diff.newHash.keyPath.nameWithExtension)
                )
                execute(target, diff, loadedContents[index]?.await()?.getOrThrow())
                return@mapIndexedNotNull diff
            } catch (executeError: Exception) {
                ensureActive()
                error(executeError) { "While apply diff $diff we have error" }
            }
            return@mapIndexedNotNull null
        }
    }

    private suspend fun execute(
        target: AbstractKeyStorage,
        diff: KeyDiff,
        content: FlipperKeyContent?
    ) {
        val path = diff.newHash.keyPath
        val folder = when (path.fileType) {
            FlipperFileType.KEY -> {
                @Suppress("UnsafeCallOnNullableType")
                path.keyType!!.flipperDir
            }

            FlipperFileType.SHADOW_NFC -> FlipperKeyType.NFC.flipperDir
            FlipperFileType.UI_INFRARED -> FlipperKeyType.INFRARED.flipperDir
            FlipperFileType.OTHER -> error("Don't support file with this type")
        }
        val targetPath = FlipperFilePath(
            folder,
            path.nameWithExtension
        )

        when (diff.action) {
            KeyAction.ADD -> target.saveFile(targetPath, requireNotNull(content))

            KeyAction.MODIFIED -> target.modify(targetPath, requireNotNull(content))

            KeyAction.DELETED -> target.deleteFile(targetPath)
        }
    }
}
