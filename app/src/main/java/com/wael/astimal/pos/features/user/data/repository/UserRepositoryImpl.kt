package com.wael.astimal.pos.features.user.data.repository

import com.wael.astimal.pos.features.user.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.UserDao
import com.wael.astimal.pos.features.user.domain.entity.User
import com.wael.astimal.pos.features.user.domain.repository.SessionManager
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class UserRepositoryImpl(
    private val userDao: UserDao,
    private val sessionManager: SessionManager
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

    override suspend fun login(
        name: String,
        password: String
    ): Result<User> {
        name.toLongOrNull()?.let {
            sessionManager.saveSession(it, "")
            userDao.getUserById(it)?.toDomain()?.let {
                return Result.success(it)
            }
        }
        TODO()
    }

}