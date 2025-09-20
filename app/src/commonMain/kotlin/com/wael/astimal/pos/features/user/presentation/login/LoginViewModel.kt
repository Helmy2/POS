package com.wael.astimal.pos.features.user.presentation.login

import androidx.lifecycle.viewModelScope
import com.wael.astimal.pos.core.base.NavigationController
import com.wael.astimal.pos.core.base.SnackbarController
import com.wael.astimal.pos.core.base.SnackbarEvent
import com.wael.astimal.pos.core.base.StringResource
import com.wael.astimal.pos.core.base.mvi.BaseViewModel
import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.launch
import pos.app.generated.resources.Res
import pos.app.generated.resources.error_login

class LoginViewModel(
    private val userRepository: UserRepository,
    private val navigationController: NavigationController,
    private val snackbarController: SnackbarController,
) : BaseViewModel<LoginContract.State, LoginContract.Event, Nothing>(
    reducer = LoginReducer(), initialState = LoginContract.State()
) {
    override fun handleEvent(event: LoginContract.Event) {
        when (event) {
            is LoginContract.Event.LoginClicked -> loginUser()
            else -> setState(event)
        }
    }

    private fun loginUser() {
        setState(LoginContract.Event.LoginClicked)

        viewModelScope.launch {
            userRepository.login(state.value.email, state.value.password).fold(
                onSuccess = {
                    navigationController.navigateAsRoot(
                        destination = Destination.Dashboard,
                    )
                },
                onFailure = {
                    it.printStackTrace()
                    setState(LoginContract.Event.LoginFailure)
                    val errorMassage = StringResource.FromResource(Res.string.error_login)
                    snackbarController.sendEvent(SnackbarEvent(errorMassage))
                },
            )
        }
    }
}
