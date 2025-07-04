package com.wael.astimal.pos.core.base

import androidx.compose.runtime.Composable
import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.presentation.theme.LocalAppLocale
import org.jetbrains.compose.resources.getString
import org.jetbrains.compose.resources.stringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.unknown_name
import java.util.Locale


sealed interface StringResource {
    class FromResource(
        val resource: org.jetbrains.compose.resources.StringResource,
        vararg val args: Any
    ) : StringResource

    class FromResourceAndLocalizedString(
        val resource: org.jetbrains.compose.resources.StringResource,
        val name: LocalizedString?,
        val formate: (resource: String, name: String) -> String
    ) : StringResource
}


@Composable
fun stringResource(resource: StringResource): String {
    val language = LocalAppLocale.current
    return when (resource) {
        is StringResource.FromResource -> stringResource(resource.resource, *resource.args)
        is StringResource.FromResourceAndLocalizedString -> resource.formate(
            stringResource(resource.resource),
            resource.name?.displayName(language) ?: stringResource(Res.string.unknown_name)
        )
    }
}

suspend fun getString(resource: StringResource): String {
    val language = Language.fromCode(Locale.getDefault().language)
    return when (resource) {
        is StringResource.FromResource -> getString(resource.resource, *resource.args)
        is StringResource.FromResourceAndLocalizedString -> resource.formate(
            getString(resource.resource),
            resource.name?.displayName(language) ?: getString(Res.string.unknown_name)
        )
    }
}