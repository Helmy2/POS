package com.wael.astimal.pos.features.user.presentation.login

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.presentation.compoenents.Screen
import com.wael.astimal.pos.features.user.presentation.components.AuthTextField
import com.wael.astimal.pos.features.user.presentation.components.CredentialsHeader
import com.wael.astimal.pos.features.user.presentation.components.PasswordTextField
import com.wael.astimal.pos.features.user.presentation.components.ProgressiveButton
import org.koin.androidx.compose.koinViewModel


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
    Screen {
        CredentialsHeader(
            title = stringResource(R.string.welcome_back),
            body = stringResource(R.string.login_to_your_account)
        )
        Spacer(modifier = Modifier.height(32.dp))

        AuthTextField(
            value = state.email,
            label = stringResource(R.string.email),
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email, imeAction = ImeAction.Next
            ),
            onValueChange = { processEvent(LoginContract.Event.EmailChanged(it)) },
            modifier = Modifier
                .sizeIn(maxWidth = 600.dp)
                .fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))
        PasswordTextField(
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

        Spacer(modifier = Modifier.height(32.dp))

        ProgressiveButton(
            text = stringResource(R.string.login),
            onClick = { processEvent(LoginContract.Event.LoginClicked) },
            isLoading = state.loading,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

