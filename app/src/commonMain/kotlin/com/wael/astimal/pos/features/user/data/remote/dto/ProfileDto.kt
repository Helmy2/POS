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
)

/**
 * Maps a ProfileDto from the network to our local database UserEntity.
 */
fun ProfileDto.toEntity(): UserEntity {
    val role = when {
        this.isAdmin -> UserRole.ADMIN
        else -> UserRole.EMPLOYEE
    }
    return UserEntity(
        id = 0L,
        supabaseId = this.id,
        username = this.username,
        enName = this.enName,
        arName = this.arName,
        email = null,
        phone = null,
        avatarUrl = this.avatarUrl,
        role = role,
        isActive = true,
        isSynced = true,
        createdAt = 0,
        updatedAt = this.updatedAt.parseIsoTimestamp() ?: System.currentTimeMillis()
    )
}
