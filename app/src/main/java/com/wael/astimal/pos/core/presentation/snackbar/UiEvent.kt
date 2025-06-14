package com.wael.astimal.pos.core.presentation.snackbar

import androidx.annotation.StringRes

sealed class UiEvent {
    data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
}