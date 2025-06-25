package com.wael.astimal.pos.features.user.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.entity.UserType

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val id: Long,
    val name: String,
    val arName: String?,
    val enName: String?,
    val email: String?,
    val phone: String?,
    val avatarUrl: String?,
    val userType: UserType,
    var isSynced: Boolean = false,
    var lastModified: Long = Clock.now(),
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        userName = name,
        name = LocalizedString(arName ?: "", enName ?: ""),
        email = email ?: "",
        phone = phone ?: "",
        // TODO change to userType
        userType = UserType.ADMIN,
        isSynced = isSynced,
        lastModified = lastModified,
        avatarUrl = avatarUrl
    )
}