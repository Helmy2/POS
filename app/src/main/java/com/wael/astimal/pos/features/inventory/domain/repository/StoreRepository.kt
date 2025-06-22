package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getStores(query: String = ""): Flow<List<Store>>
    suspend fun getStoreByLocalId(localId: Long): Store?
    suspend fun saveStore(store: StoreEntity): Result<Unit>
    suspend fun deleteStore(store: Store): Result<Unit>
}
