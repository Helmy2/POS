package com.wael.astimal.pos.features.user.data.remote.dto

import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.UserRole
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a user profile object fetched from the Supabase 'profiles' table.
 */
@Serializable
data class ProfileDto(
    val id: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val username: String,
    @SerialName("ar_name")
    val arName: String?,
    @SerialName("en_name")
    val enName: String?,
    @SerialName("avatar_url")
    val avatarUrl: String?,
    @SerialName("is_admin")
    val isAdmin: Boolean,
    @SerialName("fcm_token")
    val fcmToken: String?,
)

/**
 * Maps a ProfileDto from the network to our local database UserEntity.
 */
fun ProfileDto.toEntity(): UserEntity {
    val role = when {
        isAdmin -> UserRole.ADMIN
        else -> UserRole.EMPLOYEE
    }
    return UserEntity(
        supabaseId = id,
        isSynced = true,
        createdAt = 0,
        updatedAt = updatedAt.parseIsoTimestamp() ?: System.currentTimeMillis(),
        username = username,
        arName = arName,
        enName = enName,
        email = null,
        phone = null,
        avatarUrl = avatarUrl,
        role = role,
        isActive = true,
        fcmToken = fcmToken
    )
}
