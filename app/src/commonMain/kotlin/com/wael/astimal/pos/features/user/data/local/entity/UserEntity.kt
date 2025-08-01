package com.wael.astimal.pos.features.user.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserRole


/**
 * Represents a user's profile in the local Room database.
 * This entity is designed to mirror the structure of the 'profiles' table in Supabase.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val id: String,
    var isSynced: Boolean = false,
    val createdAt: Long,
    var updatedAt: Long,
    var isDeletedLocally: Boolean = false,
    val username: String,
    val arName: String?,
    val enName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val role: UserRole,
    val isActive: Boolean,
    val fcmToken: String?,
    val canHandlePrivatePartner: Boolean?,
)

/**
 * Maps the local UserEntity from the database to the User domain model
 * used throughout the application's business logic.
 */
fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = username,
        localizedName = LocalizedString(arName = arName, enName = enName),
        email = email,
        phone = phone,
        isAdmin = role == UserRole.ADMIN,
        isEmployee = role == UserRole.EMPLOYEE,
        avatarUrl = avatarUrl,
        isSynced = isSynced,
        updatedAt = updatedAt,
        createdAt = createdAt,
        fcmToken = fcmToken,
        canHandlePrivatePartner = canHandlePrivatePartner == true
    )
}

/**
 * Maps a User domain model back to a UserEntity for saving to the database.
 */
fun User.toEntity(): UserEntity {
    return UserEntity(
        id = id,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        username = name,
        arName = localizedName.arName,
        enName = localizedName.enName,
        email = email,
        phone = phone,
        avatarUrl = avatarUrl,
        role = if (isAdmin) UserRole.ADMIN else UserRole.EMPLOYEE,
        isActive = true,
        fcmToken = fcmToken,
        canHandlePrivatePartner = canHandlePrivatePartner
    )
}
