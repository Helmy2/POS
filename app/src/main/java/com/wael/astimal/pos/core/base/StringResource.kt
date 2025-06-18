package com.wael.astimal.pos.core.base

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wael.astimal.pos.core.base.StringResource.FromResource


/**
 * A sealed interface to represent text that can be resolved in the UI layer.
 * This makes the ViewModel platform-agnostic and KMP-ready.
 */
sealed interface StringResource {
    /**
     * Represents a string from Android's string resources.
     * @param id The resource ID of the string (e.g., R.string.error_login).
     * @param args The optional formatting arguments for the string.
     */
    class FromResource(@StringRes val id: Int, vararg val args: Any) : StringResource
}

/**
 * Resolves the StringResource to a displayable string within a Composable context.
 */
@Composable
fun stringResource(resource: StringResource): String {
    return when (resource) {
        is FromResource -> stringResource(resource.id, *resource.args)
    }
}

/**
 * Resolves the StringResource to a displayable string using a standard Android Context.
 * Useful for non-Composable parts of the UI like Toasts or Notifications.
 */
fun Context.getString(resource: StringResource): String {
    return when (resource) {
        is FromResource -> getString(resource.id, *resource.args)
    }
}