package dev.namelessnanashi.flipperzero.singleactivity.impl.utils

import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.unhandledexception.api.UnhandledExceptionApi
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

class OnCreateHandlerDispatcher @Inject constructor(
    private val unhandledExceptionApiProvider: Provider<UnhandledExceptionApi>,
) : LogTagProvider {
    override val TAG = "OnCreateHandlerDispatcher"

    fun onCreate() {
        try {
            unhandledExceptionApiProvider.invoke().initExceptionHandler()
        } catch (@Suppress("TooGenericExceptionCaught") throwable: Throwable) {
            error(throwable) { "Failed init unhandledExceptionApi" }
        }
    }
}
