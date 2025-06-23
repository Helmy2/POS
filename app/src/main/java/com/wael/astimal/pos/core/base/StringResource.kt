package com.wael.astimal.pos.core.base

import android.content.Context
import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import java.util.Locale


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


    /**
     * Represents a formatted string that combines a resource template with a LocalizedString.
     * Example: StringResource.FromResourceAndLocalizedString(R.string.not_enough_stock_for, product.name)
     */
    class FromResourceAndLocalizedString(
        @StringRes val id: Int,
        val name: LocalizedString?,
        val formate: (resourse: String, name: String) -> String
    ) : StringResource
}

/**
 * Resolves the StringResource to a displayable string within a Composable context.
 */
@Composable
fun stringResource(resource: StringResource): String {
    val language = LocalAppLocale.current
    return when (resource) {
        is StringResource.FromResource -> stringResource(resource.id, *resource.args)
        is StringResource.FromResourceAndLocalizedString -> resource.formate(
            stringResource(resource.id),
            resource.name?.displayName(language) ?: stringResource(R.string.unknown_name)
        )
    }
}

/**
 * Resolves the StringResource to a displayable string using a standard Android Context.
 * Useful for non-Composable parts of the UI like Toasts or Notifications.
 */
fun Context.getString(resource: StringResource): String {
    val language = Language.fromCode(Locale.getDefault().language)
    return when (resource) {
        is StringResource.FromResource -> getString(resource.id, *resource.args)
        is StringResource.FromResourceAndLocalizedString -> resource.formate(
            getString(resource.id),
            resource.name?.displayName(language) ?: getString(R.string.unknown_name)
        )
    }
}