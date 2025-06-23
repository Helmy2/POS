package com.wael.astimal.pos.features.user.domain.repository

import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getEmployeesFlow(): Flow<Result<List<User>>>
    suspend fun getStoreIdForEmployee(employeeId: Long): Result<Long>
    suspend fun login(email: String, password: String): Result<User>
    suspend fun isUserLoggedIn(): Boolean
    suspend fun getCurrentUser(): User?
    suspend fun logout()
}