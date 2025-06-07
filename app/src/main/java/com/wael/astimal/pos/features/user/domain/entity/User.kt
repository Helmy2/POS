package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString


data class User(
    val localId: Long,
    val serverId: Int?,
    val localizedName: LocalizedString,
    val email: String,
    val phone: String,
    val userType: UserType,
    val isSynced: Boolean = false,
    val lastModified: Long,
    val name: String,
)

enum class UserType {
    CLIENT, EMPLOYEE, UnKNOWN, ADMIN;

    companion object {
        fun fromString(value: String): UserType {
            return when (value.lowercase()) {
                "client" -> CLIENT
                "employee" -> EMPLOYEE
                "admin" -> ADMIN
                "unknown" -> UnKNOWN
                else -> throw IllegalArgumentException("Unknown user type: $value")
            }
        }
    }
}

