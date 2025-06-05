package com.wael.astimal.pos.features.inventory.domain.repository

import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import kotlinx.coroutines.flow.Flow

interface StoreRepository {
    fun getStores(query: String): Flow<List<Store>>
    suspend fun getStoreByLocalId(localId: Long): Store?
    suspend fun getStoreByServerId(serverId: Int): Store?
    suspend fun addStore(arName: String?, enName: String?, type: StoreType): Result<Unit>
    suspend fun updateStore(store: Store, newArName: String?, newEnName: String?, newType: StoreType): Result<Unit>
    suspend fun deleteStore(store: Store): Result<Unit>
}
