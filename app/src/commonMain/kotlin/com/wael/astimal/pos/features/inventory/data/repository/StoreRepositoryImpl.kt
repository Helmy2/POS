package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class StoreRepositoryImpl(
    private val storeDao: StoreDao,
) : StoreRepository {

    override fun getStores(query: String): Flow<List<Store>> {
        return storeDao.searchStoresFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreByLocalId(id: Id): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreByLocalId(id.local)
            if (entity?.isDeletedLocally == true) throw IllegalStateException("Store with localId ${id.local} is marked as deleted locally.")
            entity?.toDomain()
                ?: throw NoSuchElementException("No store found with localId ${id.local}.")
        }
    }

    override suspend fun getStoreBySeverId(id: Long): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreByServerId(id)
            if (entity?.isDeletedLocally == true) throw IllegalStateException("Store with server id $id is marked as deleted locally.")
            entity?.toDomain()
                ?: throw NoSuchElementException("No store found with server id $id.")
        }
    }

    override suspend fun saveStore(store: Store): Result<Long> {
        return runCatching {
            if (store.name.arName.isNullOrBlank() && store.name.arName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("Arabic and English names must be provided."))
            }

            storeDao.insertOrUpdate(store.toEntity())
        }
    }

    override suspend fun deleteStore(store: Store): Result<Unit> {
        return runCatching {
            val storeToMarkAsDeleted = store.toEntity().copy(
                isDeletedLocally = true, isSynced = false, updatedAt = Clock.now()
            )
            storeDao.insertOrUpdate(storeToMarkAsDeleted)
            Result.success(Unit)
        }
    }

    override suspend fun syncWithServer(stores: List<StoreEntity>) {
        val entitiesToUpsert = stores.map { serverEntity ->
            val existingLocal = serverEntity.serverId?.let { storeDao.getStoreByServerId(it) }
            serverEntity.copy(
                localId = existingLocal?.localId ?: 0L,
                createdAt = existingLocal?.createdAt ?: serverEntity.createdAt
            )
        }
        storeDao.upsertAll(entitiesToUpsert)
    }
}