package com.wael.astimal.pos.features.user.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.features.user.presentation.components.AuthTextField
import com.wael.astimal.pos.features.user.presentation.components.CredentialsHeader
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import com.wael.astimal.pos.features.user.presentation.components.ProgressiveButton
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel
import pos.app.generated.resources.Res
import pos.app.generated.resources.email
import pos.app.generated.resources.login
import pos.app.generated.resources.login_to_your_account
import pos.app.generated.resources.password
import pos.app.generated.resources.welcome_back


@Composable
fun LoginRoute(
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreen(
        state = state,
        processEvent = viewModel::processEvent
    )
}

@Composable
fun LoginScreen(state: LoginContract.State, processEvent: (LoginContract.Event) -> Unit) {
    val focus = LocalFocusManager.current
    Screen(
        modifier = Modifier.padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Spacer(modifier = Modifier.weight(1f))
        CredentialsHeader(
            title = stringResource(Res.string.welcome_back),
            body = stringResource(Res.string.login_to_your_account)
        )
        Spacer(modifier = Modifier.height(8.dp))

        AuthTextField(
            value = state.email,
            label = stringResource(Res.string.email),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            ),
            onValueChange = { processEvent(LoginContract.Event.EmailChanged(it)) },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth()
        )
        PasswordTextField(
            label = stringResource(Res.string.password),
            value = state.password,
            isVisible = state.isPasswordVisible,
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done,
            ),
            onValueChange = { processEvent(LoginContract.Event.PasswordChanged(it)) },
            onVisibilityToggle = { processEvent(LoginContract.Event.TogglePasswordVisibility) },
            onDone = {
                focus.clearFocus()
                processEvent(LoginContract.Event.LoginClicked)
            },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth(),
        )
        Spacer(modifier = Modifier.height(8.dp))

        ProgressiveButton(
            text = stringResource(Res.string.login),
            onClick = { processEvent(LoginContract.Event.LoginClicked) },
            isLoading = state.loading,
            modifier = Modifier.sizeIn(maxWidth = 600.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.weight(2f))
    }
}

