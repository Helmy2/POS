package com.wael.astimal.pos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.core.util.ConnectivityImp
import com.wael.astimal.pos.core.util.PREFERENCES_NAME
import com.wael.astimal.pos.core.data.DummyDataSeeder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.dsl.module

val coreModule = module {
    single<Connectivity> {
        ConnectivityImp(context = androidApplication())
    }
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidApplication().filesDir.resolve(PREFERENCES_NAME).absolutePath.toPath()
            }
        )
    }
    single<AppDatabase> {
        val dbFile = androidApplication().getDatabasePath("pos.db")
        Room.databaseBuilder(
            androidApplication(),
            AppDatabase::class.java,
            dbFile.absolutePath
        ).fallbackToDestructiveMigration(true)
            .setQueryCoroutineContext(Dispatchers.IO).build()
    }

    single {
        DummyDataSeeder(
            userDao = get(),
            clientDao = get(),
            storeDao = get(),
            unitDao = get(),
            categoryDao = get(),
            productDao = get(),
            stockTransferDao = get(),
            supplierDao = get(),
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
            employeeDao = get()
        )
    }
}