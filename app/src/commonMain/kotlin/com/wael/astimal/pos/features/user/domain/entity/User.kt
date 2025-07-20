package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.core.domain.entity.LocalizedString

/**
 * Represents a user or employee in the application's domain layer.
 * This is a clean data class, independent of any database or network implementation.
 */
data class User(
    val id: String,
    val name: String,
    val localizedName: LocalizedString,
    val email: String?,
    val phone: String?,
    val isAdmin: Boolean,
    val isEmployee: Boolean,
    val avatarUrl: String?,
    val fcmToken: String?,
    val isSynced: Boolean,
    val createdAt: Long,
    val updatedAt: Long,
)

