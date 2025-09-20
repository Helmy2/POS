package com.wael.astimal.pos.features.user.domain.repository

import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.PermissionDetails
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getEmployeesFlow(): Flow<List<User>>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
    suspend fun getUserByServerId(id: String): Result<User?>
    suspend fun syncWithServer(users: List<UserEntity>): Result<Unit>
    suspend fun createUser(
        email: String,
        arName: String,
        enName: String,
        password: String,
        canHandlePrivatePartner: Boolean,
        permissions: Map<String, PermissionDetails>
    ): Result<Unit>

    suspend fun deleteUser(id: String): Result<Unit>
    suspend fun updateUser(
        id: String,
        arName: String,
        enName: String,
        password: String?,
        email: String?,
        canHandlePrivatePartner: Boolean,
        permissions: Map<String, PermissionDetails>
    ): Result<Unit>

    suspend fun getAdmin(): User?

    suspend fun deleteAll(ids: List<String>): Result<Unit>
}