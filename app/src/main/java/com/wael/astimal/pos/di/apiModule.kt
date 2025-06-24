package com.wael.astimal.pos.di

import com.wael.astimal.pos.core.util.ApiRoutes
import com.wael.astimal.pos.features.user.data.local.SessionManager
import com.wael.astimal.pos.features.user.data.remote.AuthApiService
import com.wael.astimal.pos.features.user.data.remote.AuthApiServiceImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module


val apiModule = module {
    singleOf(::AuthApiServiceImpl) { bind<AuthApiService>() }

    single {
        val sessionManager = get<SessionManager>()

        HttpClient(Android) {
            install(Logging) {
                level = LogLevel.ALL
            }
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                })
            }
            install(Auth) {
                bearer {
                    loadTokens { sessionManager.getBearerTokens() }

                    refreshTokens { sessionManager.refreshBearerTokens(client) }

                    sendWithoutRequest { request ->
                        val url = request.url.encodedPath
                        request.url.host == ApiRoutes.HOST &&
                                url != ApiRoutes.LOGIN
                    }
                }
            }
        }
    }
}
