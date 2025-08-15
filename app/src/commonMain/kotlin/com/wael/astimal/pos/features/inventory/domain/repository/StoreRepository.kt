package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getStores(query: String = ""): Flow<List<Store>>
    suspend fun getStoreById(id: String): Result<Store>
    suspend fun saveStore(store: Store): Result<Unit>
    suspend fun deleteStore(store: Store): Result<Unit>
    suspend fun syncWithServer(stores: List<StoreEntity>): Result<Unit>
    fun getStoresForUser(user: User): Flow<List<Store>>
    suspend fun deleteAll(ids: List<String>): Result<Unit>
}
