package com.wael.astimal.pos.features.user.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.admin
import pos.app.generated.resources.employee
import pos.app.generated.resources.unknown

/**
 * Represents a user or employee in the application's domain layer.
 * This is a clean data class, independent of any database or network implementation.
 */
data class User(
    override val id: Id,
    val name: String,
    val localizedName: LocalizedString,
    val email: String?,
    val phone: String?,
    val isAdmin: Boolean,
    val isEmployee: Boolean,
    val avatarUrl: String?,
    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long,
) : Item

enum class UserType {
    ADMIN, EMPLOYEE, UNKNOWN;

    fun stringResource(type: UserType = this): StringResource {
        return when (type) {
            EMPLOYEE -> Res.string.employee
            ADMIN -> Res.string.admin
            UNKNOWN -> Res.string.unknown
        }
    }
}

