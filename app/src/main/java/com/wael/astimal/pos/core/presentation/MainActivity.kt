package com.wael.astimal.pos.core.presentation

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.MainScaffold
import com.wael.astimal.pos.core.presentation.theme.POSTheme
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import org.koin.android.ext.android.inject

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        val splashScreen = installSplashScreen()
        val startDestination: MutableStateFlow<Result<Destination>?> = MutableStateFlow(null)
        val sessionManager: SessionManager by inject()

        lifecycleScope.launch {
            sessionManager.getCurrentUser().collectLatest {
                startDestination.value = if (it == null) {
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
            val state = startDestination.collectAsStateWithLifecycle()
            state.value?.let {
                POSTheme {
                    it.onSuccess {
                        MainScaffold(it)
                    }.onFailure {
                        Box(
                            contentAlignment = Alignment.Center,
                            modifier = Modifier.fillMaxSize()
                        ) {
                            Text(stringResource(R.string.something_went_wrong))
                        }
                    }
                }
            }
        }
    }
}