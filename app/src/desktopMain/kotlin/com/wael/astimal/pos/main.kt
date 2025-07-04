package com.wael.astimal.pos

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.data.SyncService
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.MainScaffold
import com.wael.astimal.pos.core.presentation.theme.POSTheme
import com.wael.astimal.pos.di.initKoin
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.koinInject
import pos.app.generated.resources.Res
import pos.app.generated.resources.something_went_wrong

fun main() {
    initKoin()
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Pos",
        ) {
            val startDestination: MutableStateFlow<Result<Destination>?> = MutableStateFlow(null)
            val userRepository: UserRepository = koinInject()
            val syncService: SyncService = koinInject()
            val coroutineScope = rememberCoroutineScope()

            coroutineScope.launch {
                userRepository.isUserLoggedIn().let {
                    startDestination.value = if (it) {
                        Result.success(Destination.Main)
                    } else {
                        Result.success(Destination.Auth)
                    }
                }
                syncService.performFullSync()
            }

            POSTheme {
                val state by startDestination.collectAsStateWithLifecycle()
                if (state == null) {
                    Box(
                        contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                    ) {
                        CircularProgressIndicator()
                    }
                } else {
                    state?.let { result ->
                        result.onSuccess {
                            MainScaffold(it)
                        }.onFailure {
                            Box(
                                contentAlignment = Alignment.Center,
                                modifier = Modifier.fillMaxSize()
                            ) {
                                Text(stringResource(Res.string.something_went_wrong))
                            }
                        }
                    }
                }
            }
        }
    }
}