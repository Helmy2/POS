package com.wael.astimal.pos.features.user.domain

import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.domain.entity.User

object PermissionManager {
    private var permissions: Map<String, PermissionDetails> = emptyMap()
    private var isAdmin: Boolean = false

    // This function should be called by your UserRepository after a user successfully logs in
    fun updatePermissions(user: User?) {
        permissions = user?.permissions ?: emptyMap()
        isAdmin = user?.isAdmin ?: false
    }

    // A helper function to get the permission key from a Destination
    private fun getKey(destination: Destination): String = destination.toString()

    // Simple, readable functions for the UI to use
    fun canView(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canView ?: false
    }

    fun canCreate(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canCreate ?: false
    }

    fun canUpdate(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canUpdate ?: false
    }

    fun canDelete(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canDelete ?: false
    }

    fun isAdmin(): Boolean {
        return isAdmin
    }
}