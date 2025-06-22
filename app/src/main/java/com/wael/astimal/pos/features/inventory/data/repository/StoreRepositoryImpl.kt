package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.StoreEntity
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

    override suspend fun saveStore(store: StoreEntity): Result<Unit> {
        return runCatching {
            if (store.arName.isBlank() && store.enName.isBlank()) {
                return Result.failure(IllegalArgumentException("At least one name (Arabic or English) must be provided."))
            }

            storeDao.insertOrUpdate(store)
        }
    }

    override suspend fun deleteStore(store: Store): Result<Unit> { // Takes domain model
        return try {
            val entityToDelete = storeDao.getStoreByLocalId(store.id.local)
            if (entityToDelete == null) {
                return Result.failure(NoSuchElementException("Store not found for deletion"))
            }
            val storeToMarkAsDeleted = entityToDelete.copy(
                isDeletedLocally = true,
                isSynced = false,
                updatedAt = System.currentTimeMillis()
            )
            storeDao.insertOrUpdate(storeToMarkAsDeleted)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}