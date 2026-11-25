package dev.namelessnanashi.flipperzero.firstpair.impl.api

import androidx.datastore.core.DataStore
import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.ktx.jre.runBlockingWithLog
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.preference.pb.Settings
import dev.namelessnanashi.flipperzero.firstpair.api.FirstPairApi
import dev.namelessnanashi.flipperzero.firstpair.impl.storage.FirstPairStorage
import dev.zacsweers.metro.ContributesBinding
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.binding
import kotlinx.coroutines.flow.first

@ContributesBinding(AppGraph::class, binding<FirstPairApi>())
class FirstPairApiImpl @Inject constructor(
    private val firstPairStorage: FirstPairStorage,
    private val settingsStore: DataStore<Settings>
) : FirstPairApi, LogTagProvider {
    override val TAG = "FirstPairApi"

    override fun shouldWeOpenPairScreen(): Boolean {
        return firstPairStorage.isDeviceSelected().not()
    }

    override fun consumeForkNoticeOnStart(): Boolean {
        if (!ForkNoticeSession.markShown()) return false
        val hidden = runBlockingWithLog("fork_notice") { settingsStore.data.first() }.hide_fork_notice_on_start
        return !hidden
    }
}

/**
 * Whether this process already showed the unofficial-build notice (the first-launch screen or
 * the start-up notice)
 */
internal object ForkNoticeSession {
    @Volatile
    private var shown = false

    /**
     * @return true if it was not shown yet
     */
    @Synchronized
    fun markShown(): Boolean {
        if (shown) return false
        shown = true
        return true
    }
}
