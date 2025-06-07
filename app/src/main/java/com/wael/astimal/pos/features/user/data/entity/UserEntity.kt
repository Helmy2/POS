package com.wael.astimal.pos.features.user.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
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
    val isAdminFlag: Boolean = false,
    val isClientFlag: Boolean = false,
    val isEmployeeFlag: Boolean = false,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis()
)

fun UserEntity.toDomain(): User {
    return User(
        id = id,
        name = name,
        localizedName = LocalizedString(arName ?: "", enName ?: ""),
        email = email ?: "",
        phone = phone ?: "",
        userType = when {
            isClientFlag -> UserType.CLIENT
            isEmployeeFlag -> UserType.EMPLOYEE
            isAdminFlag -> UserType.ADMIN
            else -> UserType.UNKNOWN
        },
        isSynced = isSynced,
        lastModified = lastModified,
    )
}