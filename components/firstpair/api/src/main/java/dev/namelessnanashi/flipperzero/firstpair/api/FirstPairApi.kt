package dev.namelessnanashi.flipperzero.firstpair.api

/**
 * Class which provide api to pair module
 */
interface FirstPairApi {
    /**
     * @return true if we already pass pair screen at least one time
     */
    fun shouldWeOpenPairScreen(): Boolean

    /**
     * Fork: true once per app start (unless switched off in Options) for the unofficial-build
     * notice; the first-launch screen already counts as shown. Calling it marks it shown.
     */
    fun consumeForkNoticeOnStart(): Boolean
}
