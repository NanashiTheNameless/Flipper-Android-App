package dev.namelessnanashi.flipperzero.faphub.dao.network.network.utils

import dev.namelessnanashi.flipperzero.core.ktx.jre.withLock
import dev.namelessnanashi.flipperzero.core.log.LogTagProvider
import dev.namelessnanashi.flipperzero.core.log.error
import dev.namelessnanashi.flipperzero.core.log.info
import dev.namelessnanashi.flipperzero.faphub.dao.api.model.FapCategory
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.api.FapNetworkCategoryApi
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.model.FapNetworkHostEnum
import dev.namelessnanashi.flipperzero.faphub.dao.network.network.model.KtorfitCategory
import dev.namelessnanashi.flipperzero.faphub.target.model.FlipperTarget
import io.ktor.client.HttpClient
import kotlinx.coroutines.sync.Mutex
import dev.zacsweers.metro.Inject
import dev.zacsweers.metro.Provider

class FapCachedCategoryApi @Inject constructor(
    httpClient: HttpClient,
    fapHostProvider: Provider<FapNetworkHostEnum>,
) : LogTagProvider {
    private val categoryApi = FapNetworkCategoryApi(httpClient, fapHostProvider)

    override val TAG = "FapHubNetworkCategoryApi"

    private val mutex = Mutex()
    private var currentTarget: FlipperTarget? = null
    private var categories: Map<String, KtorfitCategory>? = null
    private var categoriesReceivedError: Throwable? = null

    suspend fun get(target: FlipperTarget, id: String): FapCategory? {
        invalidateIfNeed(target)
        return categories?.get(id)?.toFapCategory()
    }

    suspend fun getAll(target: FlipperTarget): List<KtorfitCategory> {
        invalidateIfNeed(target)
        return categories?.map { it.value }
            ?: categoriesReceivedError?.let { throw it }
            ?: error("Failed receive categories")
    }

    private suspend fun invalidateIfNeed(target: FlipperTarget) = withLock(mutex, "invalidate") {
        if (!shouldInvalidate(target)) {
            return@withLock
        }
        categoriesReceivedError = null
        var categoriesReceived = try {
            categoryApi.getAll(
                sdkApi = when (target) {
                    is FlipperTarget.Received -> target.sdk.toString()
                    FlipperTarget.Unsupported,
                    FlipperTarget.NotConnected -> null
                },
                target = when (target) {
                    is FlipperTarget.Received -> target.target
                    FlipperTarget.Unsupported,
                    FlipperTarget.NotConnected -> null
                }
            )
        } catch (ex: Exception) {
            error(ex) { "Failed get categories" }
            categoriesReceivedError = ex
            return@withLock
        }
        if (target is FlipperTarget.Unsupported) {
            categoriesReceived = categoriesReceived.map { it.copy(applicationsCount = 0) }
        }
        info { "Received ${categoriesReceived.size} categories" }
        categories = categoriesReceived.associateBy { it.id }
        currentTarget = target
    }

    private fun shouldInvalidate(target: FlipperTarget): Boolean {
        if (currentTarget != target) {
            return true
        }
        if (categories == null) {
            return true
        }
        info { "Skip invalidate because target is same and categories exist" }
        return false
    }
}
