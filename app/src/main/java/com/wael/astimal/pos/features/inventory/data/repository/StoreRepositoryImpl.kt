package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.entity.StoreType
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.domain.entity.Store
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

    override suspend fun getStoreByLocalId(localId: Long): Store? {
        val entity = storeDao.getStoreByLocalId(localId)
        return if (entity?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun getStoreByServerId(serverId: Int): Store? {
        val entity = storeDao.getStoreByServerId(serverId)
        return if (entity?.isDeletedLocally == true) null else entity?.toDomain()
    }

    override suspend fun addStore(
        arName: String?,
        enName: String?,
        type: StoreType
    ): Result<Unit> {
        return try {
            if (arName.isNullOrBlank() && enName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided."))
            }

            val newStoreEntity = StoreEntity(
                localId = 0L,
                serverId = null,
                arName = arName,
                enName = enName,
                type = type,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = false
            )
            storeDao.insertStores(listOf(newStoreEntity))
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun updateStore(
        store: Store,
        newArName: String?,
        newEnName: String?,
        newType: StoreType
    ): Result<Unit> {
        return try {
            if (newArName.isNullOrBlank() && newEnName.isNullOrBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided."))
            }

            val entityToUpdate = StoreEntity(
                localId = store.localId,
                serverId = store.serverId,
                arName = newArName,
                enName = newEnName,
                type = newType,
                isSynced = false,
                lastModified = System.currentTimeMillis(),
                isDeletedLocally = store.isDeletedLocally
            )
            storeDao.updateStore(entityToUpdate)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun deleteStore(store: Store): Result<Unit> { // Takes domain model
        return try {
            val entityToDelete = storeDao.getStoreByLocalId(store.localId)
            if (entityToDelete == null) {
                return Result.failure(NoSuchElementException("Store not found for deletion"))
            }
            val storeToMarkAsDeleted = entityToDelete.copy(
                isDeletedLocally = true,
                isSynced = false,
                lastModified = System.currentTimeMillis()
            )
            storeDao.updateStore(storeToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }


    override suspend fun syncStores(): Result<Unit> {
        // todo: Implement the logic to sync stores with the remote API.
        println("StoreRepositoryImpl: syncStores() called, API service not yet integrated.")
        return Result.success(Unit)
    }
}