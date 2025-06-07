package com.wael.astimal.pos.features.client_management.data.repository

import com.wael.astimal.pos.features.client_management.data.entity.toDomain
import com.wael.astimal.pos.features.client_management.data.local.ClientDao
import com.wael.astimal.pos.features.client_management.domain.entity.Client
import com.wael.astimal.pos.features.client_management.domain.repository.ClientRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientRepositoryImpl(private val clientDao: ClientDao) :
    ClientRepository {
    override fun searchClients(query: String): Flow<List<Client>> {
        return clientDao.searchClientsWithDetailsFlow(query).map { list ->
            list.map { it.toDomain() }
        }
    }
}

