package com.wael.astimal.pos.di

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import androidx.datastore.preferences.core.Preferences
import com.wael.astimal.pos.core.util.PREFERENCES_NAME
import com.wael.astimal.pos.features.user.data.repository.SessionManagerImpl
import com.wael.astimal.pos.features.user.data.repository.SettingsManagerImpl
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.SettingsManager
import com.wael.astimal.pos.features.user.presentation.login.LoginViewModel
import com.wael.astimal.pos.features.user.presentation.setting.SettingsViewModel
import okio.Path.Companion.toPath
import org.koin.android.ext.koin.androidApplication
import org.koin.core.module.dsl.viewModel
import org.koin.dsl.module

val userModule = module {
    single<DataStore<Preferences>> {
        PreferenceDataStoreFactory.createWithPath(
            produceFile = {
                androidApplication().filesDir.resolve(PREFERENCES_NAME).absolutePath.toPath()
            }
        )
    }

    single<SettingsManager> {
        SettingsManagerImpl(get())
    }
    single<SessionManager> {
        SessionManagerImpl(get())
    }

    viewModel { SettingsViewModel( get(),get()) }
    viewModel { LoginViewModel(get()) }
}