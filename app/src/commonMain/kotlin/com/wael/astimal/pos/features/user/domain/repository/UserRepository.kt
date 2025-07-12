package com.wael.astimal.pos.features.user.domain.repository

import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getEmployeesFlow(): Flow<List<User>>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun logout(): Result<Unit>
    suspend fun getUserByServerId(id: String): Result<User?>
    suspend fun getUserById(id: Long): Result<User?>
    suspend fun syncWithServer(users: List<UserEntity>): Result<Unit>
}