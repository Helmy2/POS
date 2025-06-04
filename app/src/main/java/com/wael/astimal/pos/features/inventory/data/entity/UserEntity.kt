package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey()
    val id: Long,
    val name: String,
    val email: String?,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis()
)
