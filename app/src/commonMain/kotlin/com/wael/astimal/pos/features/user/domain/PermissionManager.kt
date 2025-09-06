package com.wael.astimal.pos.features.user.domain

import com.wael.astimal.pos.core.domain.navigation.Destination
import com.wael.astimal.pos.core.presentation.navigation.AppKoinComponent.getKoin
import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch

object PermissionManager {
    private var permissions: Map<String, PermissionDetails> = emptyMap()
    private var isAdmin: Boolean = false

    private val connectivity = getKoin().get<Connectivity>()
    private var isConnected = false

    init {
        GlobalScope.launch {
            connectivity.statusUpdates.collect {
                isConnected = it.isConnected
            }
        }
    }

    // This function should be called by your UserRepository after a user successfully logs in
    fun updatePermissions(user: User?) {
        permissions = user?.permissions ?: emptyMap()
        isAdmin = user?.isAdmin ?: false
    }

    // A helper function to get the permission key from a Destination
    private fun getKey(destination: Destination): String = destination.key

    // Simple, readable functions for the UI to use
    fun canView(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canView ?: false
    }

    fun canCreate(destination: Destination): Boolean {
        return permissions[getKey(destination)]?.canCreate ?: false
    }

    fun canUpdate(destination: Destination): Boolean {
        if (!isConnected) return false
        return permissions[getKey(destination)]?.canUpdate ?: false
    }

    fun canDelete(destination: Destination): Boolean {
        if (!isConnected) return false
        return permissions[getKey(destination)]?.canDelete ?: false
    }

    fun isAdmin(): Boolean {
        return isAdmin
    }
}