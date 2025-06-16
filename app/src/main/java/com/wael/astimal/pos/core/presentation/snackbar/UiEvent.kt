package com.wael.astimal.pos.core.presentation.snackbar

import android.net.Uri
import androidx.annotation.StringRes

sealed class UiEvent {
    data class ShowSnackbar(@StringRes val message: Int) : UiEvent()
    data class ShareFile(val fileUri: Uri, @StringRes val fileTitle: Int) : UiEvent()
}