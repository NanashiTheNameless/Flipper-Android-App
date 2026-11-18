package dev.namelessnanashi.flipperzero.bridge.connection.feature.rpc.impl

private const val DEFAULT_THRESHOLD = 3
private const val DEFAULT_WINDOW_MS = 30_000L
private const val DEFAULT_COOLDOWN_MS = 5 * 60_000L

/**
 * Two apps connected to the same Flipper share one RPC session, so each one receives the
 * other's answers. Answers to command ids this app never sent are the only visible sign.
 * Reports when [threshold] of them arrive within [windowMs], at most once per [cooldownMs].
 */
internal class ForeignClientDetector(
    private val threshold: Int = DEFAULT_THRESHOLD,
    private val windowMs: Long = DEFAULT_WINDOW_MS,
    private val cooldownMs: Long = DEFAULT_COOLDOWN_MS
) {
    private val recent = ArrayDeque<Long>()
    private var lastReportMs: Long? = null

    /**
     * @return true when the user should be warned now
     */
    @Synchronized
    fun onUnknownAnswer(nowMs: Long): Boolean {
        recent.addLast(nowMs)
        while (recent.isNotEmpty() && nowMs - recent.first() > windowMs) {
            recent.removeFirst()
        }
        val last = lastReportMs
        if (recent.size < threshold || (last != null && nowMs - last < cooldownMs)) {
            return false
        }
        lastReportMs = nowMs
        recent.clear()
        return true
    }
}
