package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString


data class User(
    val id: Long,
    val localizedName: LocalizedString,
    val email: String,
    val phone: String,
    val userType: UserType,
    val isSynced: Boolean = false,
    val lastModified: Long,
    val name: String,
    val avatarUrl: String?,
) {
    val isEmployee: Boolean get() = userType == UserType.EMPLOYEE
    val isAdmin: Boolean get() = userType == UserType.ADMIN
}

enum class UserType {
    ADMIN, EMPLOYEE, UNKNOWN;

    fun stringResource(type: UserType = this): Int {
        return when (type) {
            EMPLOYEE -> com.wael.astimal.pos.R.string.employee
            ADMIN -> com.wael.astimal.pos.R.string.admin
            UNKNOWN -> com.wael.astimal.pos.R.string.unknown
        }
    }
}

