package com.wael.astimal.pos.features.user.domain.repository

import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun getEmployeesFlow(): Flow<List<User>>
    fun getPotentialClientsFlow(query: String): Flow<List<User>>
    suspend fun getUser(localId: Long): User?
}