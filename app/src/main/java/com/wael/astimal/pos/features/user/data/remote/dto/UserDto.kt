package com.wael.astimal.pos.features.user.data.remote.dto

import com.wael.astimal.pos.core.util.PROFILE_IMAGE_BASE_URL
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.UserType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class UserDto(
    @SerialName("id")
    val id: Long,
    @SerialName("name")
    val name: String,
    @SerialName("is_admin")
    val isAdmin: Int,
    @SerialName("is_employee")
    val isEmployee: Int,
    @SerialName("user_name")
    val userName: String,
    @SerialName("email")
    val email: String?,
    @SerialName("phone")
    val phone: String?,
    @SerialName("avatar")
    val avatar: String?,
    @SerialName("is_block")
    val isBlock: Int
)

fun UserDto.toEntity(
    hashedPassword: String
): UserEntity {
    val role = when {
        isAdmin == 1 -> UserType.ADMIN
        isEmployee == 1 -> UserType.EMPLOYEE
        else -> UserType.UNKNOWN
    }
    return UserEntity(
        id = id,
        enName = name,
        name = userName,
        arName = null,
        userType = role,
        email = email,
        phone = phone,
        avatarUrl = PROFILE_IMAGE_BASE_URL + avatar,
        hashedPassword = hashedPassword
    )
}
