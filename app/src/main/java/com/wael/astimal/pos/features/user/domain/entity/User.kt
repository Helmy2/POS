package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString


data class User(
    val id: Long,
    val localizedName: LocalizedString,
    val email: String,
    val phone: String,
    val userType: UserType,
    val isSynced: Boolean = false,
    val lastModified: Long,
    val name: String,
)

enum class UserType {
    CLIENT, EMPLOYEE, UNKNOWN, ADMIN;
}

