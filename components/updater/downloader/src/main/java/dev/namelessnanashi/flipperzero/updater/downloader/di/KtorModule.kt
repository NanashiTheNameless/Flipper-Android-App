package dev.namelessnanashi.flipperzero.updater.downloader.di

import dev.namelessnanashi.flipperzero.core.di.AppGraph
import dev.namelessnanashi.flipperzero.core.buildkonfig.BuildKonfig
import dev.namelessnanashi.flipperzero.core.log.TaggedLogger
import dev.namelessnanashi.flipperzero.core.log.verbose
import dev.zacsweers.metro.ContributesTo
import dev.zacsweers.metro.Provides
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.DefaultRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.header
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import dev.zacsweers.metro.SingleIn

@ContributesTo(AppGraph::class)
interface KtorModule {
    @Provides
    @SingleIn(AppGraph::class)
    fun providerKtorClient(): HttpClient {
        val ktorTimber = TaggedLogger("Ktor")

        return HttpClient(Android) {
            expectSuccess = true
            install(ContentNegotiation) {
                json(
                    Json {
                        prettyPrint = true
                        isLenient = true
                        ignoreUnknownKeys = true
                        coerceInputValues = true
                    }
                )
            }

            if (BuildKonfig.IS_LOG_ENABLED) {
                install(Logging) {
                    logger = object : Logger {
                        override fun log(message: String) {
                            ktorTimber.verbose { message }
                        }
                    }
                    level = if (BuildKonfig.IS_VERBOSE_LOG_ENABLED) LogLevel.ALL else LogLevel.INFO
                }
            }

            install(DefaultRequest) {
                header(HttpHeaders.ContentType, ContentType.Application.Json)
            }
        }
    }
}
