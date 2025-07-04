package com.wael.astimal.pos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.ApiRoutes
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.core.util.ConnectivityImp
import com.wael.astimal.pos.core.util.PREFERENCES_NAME
import com.wael.astimal.pos.core.util.PdfGenerator
import com.wael.astimal.pos.core.util.PdfGeneratorImpl
import com.wael.astimal.pos.features.user.data.local.SessionManager
import io.ktor.client.HttpClient
import io.ktor.client.engine.android.Android
import io.ktor.client.plugins.auth.Auth
import io.ktor.client.plugins.auth.providers.BearerTokens
import io.ktor.client.plugins.auth.providers.bearer
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.http.encodedPath
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

actual val platformModule: Module = module {
    single<Connectivity> { ConnectivityImp(context = androidApplication()) }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidApplication().filesDir.resolve(PREFERENCES_NAME).absolutePath.toPath()
            },
        )
    }
    single<AppDatabase> {
        val dbFile = androidApplication().getDatabasePath("pos.db")
        Room.databaseBuilder(
            androidApplication(), AppDatabase::class.java, dbFile.absolutePath
        ).fallbackToDestructiveMigration(true).setQueryCoroutineContext(Dispatchers.IO).build()
    }

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
                    loadTokens {
                        BearerTokens(
                            sessionManager.getAccessToken(),
                            null
                        )
                    }

                    refreshTokens { sessionManager.refreshBearerTokens(client) }

                    sendWithoutRequest { request ->
                        request.url.encodedPath == ApiRoutes.LOGIN
                    }
                }
            }
        }
    }

    singleOf(::PdfGeneratorImpl) { bind<PdfGenerator>() }
}