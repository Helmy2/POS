package com.wael.astimal.pos

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.mmk.kmpnotifier.permission.permissionUtil
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.MainScaffold
import com.wael.astimal.pos.core.presentation.theme.POSTheme
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.jetbrains.compose.resources.stringResource
import org.koin.android.ext.android.inject
import pos.app.generated.resources.Res
import pos.app.generated.resources.something_went_wrong

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        val startDestination: MutableStateFlow<Result<Destination>?> = MutableStateFlow(null)
        val userRepository: UserRepository by inject()
        val permissionUtil by permissionUtil()

        permissionUtil.askNotificationPermission()

        lifecycleScope.launch {
            userRepository.isUserLoggedIn().let {
                startDestination.value = if (it) {
                    Result.success(Destination.Main)
                } else {
                    Result.success(Destination.Auth)
                }
            }
        }

        splashScreen.setKeepOnScreenCondition {
            startDestination.value == null
        }

        setContent {
            POSTheme {
                val state = startDestination.collectAsStateWithLifecycle()
                state.value?.let {
                    it.onSuccess {
                        MainScaffold(it)
                    }.onFailure {
                        Box(
                            contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()
                        ) {
                            Text(stringResource(Res.string.something_went_wrong))
                        }
                    }
                }
            }
        }
    }
}