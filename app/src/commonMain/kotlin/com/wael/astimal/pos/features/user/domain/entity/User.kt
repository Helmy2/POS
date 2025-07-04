package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.admin
import pos.app.generated.resources.employee
import pos.app.generated.resources.unknown


data class User(
    val id: Long,
    val name: LocalizedString,
    val email: String,
    val phone: String,
    val userType: UserType,
    val isSynced: Boolean = false,
    val lastModified: Long,
    val userName: String,
    val avatarUrl: String?,
) {
    val isEmployee: Boolean get() = userType == UserType.EMPLOYEE
    val isAdmin: Boolean get() = userType == UserType.ADMIN
}

enum class UserType {
    ADMIN, EMPLOYEE, UNKNOWN;

    fun stringResource(type: UserType = this): StringResource {
        return when (type) {
            EMPLOYEE -> Res.string.employee
            ADMIN -> Res.string.admin
            UNKNOWN -> Res.string.unknown
        }
    }
}

