package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.Client
import kotlinx.coroutines.flow.Flow


interface ClientRepository {
    fun searchClients(query: String=""): Flow<List<Client>>
    suspend fun adjustClientDebt(clientLocalId: Long, changeInDebt: Double)
    suspend fun getClient(clientId: Long): Client?
}