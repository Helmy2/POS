package com.wael.astimal.pos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import androidx.room.Room
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.core.data.DummyDataSeeder
import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.data.SyncManagerImpl
import com.wael.astimal.pos.core.data.SyncService
import com.wael.astimal.pos.core.data.SyncServiceImpl
import com.wael.astimal.pos.core.data.remote.SyncApiService
import com.wael.astimal.pos.core.data.remote.SyncApiServiceImpl
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.core.util.ConnectivityImp
import com.wael.astimal.pos.core.util.PREFERENCES_NAME
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.bind
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.module

val coreModule = module {
    single<SnackbarController> { SnackbarController }
    single<NavigationController> { NavigationController }

    single<Connectivity> { ConnectivityImp(context = androidApplication()) }

    singleOf(::SyncApiServiceImpl) { bind<SyncApiService>() }
    singleOf(::SyncManagerImpl) { bind<SyncManager>() }
    singleOf(::SyncServiceImpl) { bind<SyncService>() }


    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidApplication().filesDir.resolve(PREFERENCES_NAME).absolutePath.toPath()
            })
    }
    single<AppDatabase> {
        val dbFile = androidApplication().getDatabasePath("pos.db")
        Room.databaseBuilder(
            androidApplication(), AppDatabase::class.java, dbFile.absolutePath
        ).fallbackToDestructiveMigration(true).setQueryCoroutineContext(Dispatchers.IO).build()
    }

    single {
        DummyDataSeeder(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.IO),
        )
    }
}