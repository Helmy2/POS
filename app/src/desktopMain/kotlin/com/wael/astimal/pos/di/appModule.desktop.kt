package com.wael.astimal.pos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import androidx.sqlite.driver.bundled.BundledSQLiteDriver
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.core.util.ConnectivityImp
import com.wael.astimal.pos.core.util.PREFERENCES_NAME
import com.wael.astimal.pos.core.util.PdfGenerator
import com.wael.astimal.pos.core.util.PdfGeneratorImpl
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import kotlinx.coroutines.Dispatchers
import kotlinx.serialization.json.Json
import okio.Path.Companion.toPath
import org.koin.core.module.Module
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module
import java.io.File

actual val platformModule: Module = module {

    single<Connectivity> { ConnectivityImp() }

    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                val file = File(System.getProperty("java.io.tmpdir"), PREFERENCES_NAME)
                file.absolutePath.toPath()
            },
        )
    }

    single<AppDatabase> {
        val dbFile = File(System.getProperty("java.io.tmpdir"), "pos.db")
        Room.databaseBuilder<AppDatabase>(
            dbFile.absolutePath,
        ).fallbackToDestructiveMigration(true)
            .setDriver(BundledSQLiteDriver())
            .setQueryCoroutineContext(Dispatchers.IO).build()
    }

    single {
        HttpClient(OkHttp) {
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
        }
    }

    singleOf(::PdfGeneratorImpl) { bind<PdfGenerator>() }
}