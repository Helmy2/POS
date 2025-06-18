package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.core.util.Connectivity
import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.Crypto
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.data.remote.AuthApiService
import com.wael.astimal.pos.features.user.data.remote.dto.LoginRequest
import com.wael.astimal.pos.features.user.data.remote.dto.toEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
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

    override suspend fun getUser(localId: Long): User? {
        return userDao.getUserById(localId)?.toDomain()
    }

    override suspend fun getStoreIdForEmployee(employeeId: Long): Long? {
        return userDao.getStoreIdForEmployee(employeeId)
    }


    override suspend fun login(email: String, password: String): Result<User> {
        if (connectivity.statusUpdates.first().isConnected) {
            val apiResult = authApiService.login(LoginRequest(email = email, password = password))
            apiResult.onSuccess { response ->
                val userEntity = response.user.toEntity(
                    Crypto.encrypt(password.toByteArray()).toString()
                )
                sessionManager.saveSession(userEntity.id, response.token)
                userDao.insertOrUpdate(userEntity)

                return Result.success(userEntity.toDomain())
            }
            apiResult.onFailure {
                return Result.failure(it)
            }
        }

        return try {
            val userEntity = userDao.findByEmail(email)
            if (userEntity != null && Crypto.encrypt(password.toByteArray())
                    .toString() == userEntity.hashedPassword
            ) {
                sessionManager.saveUserId(userEntity.id)
                Result.success(userEntity.toDomain())
            } else {
                Result.failure(Exception("Invalid username or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

}