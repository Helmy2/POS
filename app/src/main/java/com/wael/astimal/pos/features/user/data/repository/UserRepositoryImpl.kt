package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.SessionManager
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.data.remote.AuthApiService
import com.wael.astimal.pos.features.user.data.remote.dto.LoginRequest
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager,
    private val authApiService: AuthApiService,
    private val connectivity: Connectivity
) : UserRepository {

    override fun getEmployeesFlow(): Flow<List<User>> {
        return userDao.getAllEmployeesFlow().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreIdForEmployee(employeeId: Long): Result<Long> {
        return runCatching {
            userDao.getStoreIdForEmployee(employeeId)
                ?: throw NoSuchElementException("No store found for employee with ID $employeeId")
        }
    }

    override suspend fun getCurrentUser(): User? {
        val userId = sessionManager.getCurrentUserId() ?: return null
        return userDao.getUserById(userId)?.toDomain()
    }

    override suspend fun logout() {
        sessionManager.clearSession()
    }

    override suspend fun isUserLoggedIn(): Boolean {
        return sessionManager.isUserLoggedIn()
    }


    override suspend fun login(email: String, password: String): Result<User> {
        return runCatching {
            if (connectivity.statusUpdates.first().isDisconnected) throw Exception("No internet connection")

            val response =
                authApiService.login(LoginRequest(email = email, password = password)).getOrThrow()
            val userEntity = response.user.toEntity()
            sessionManager.saveUserSession(
                userId = userEntity.id,
                email = email,
                password = password,
                accessToken = response.token,
            )
            userDao.insertOrUpdate(userEntity)

            userEntity.toDomain()
        }
    }
}