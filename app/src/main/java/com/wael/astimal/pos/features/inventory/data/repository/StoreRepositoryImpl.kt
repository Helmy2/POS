package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.map


class StoreRepositoryImpl(
    private val storeDao: StoreDao,
) : StoreRepository {

    override fun getStores(query: String): Flow<Result<List<Store>>> {
        return storeDao.searchStoresFlow(query).map { entities ->
            runCatching { entities.map { it.toDomain() } }
        }.catch { emit(Result.failure(it)) }
    }

    override suspend fun getStoreByLocalId(localId: Long): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreByLocalId(localId)
            if (entity?.isDeletedLocally == true) throw IllegalStateException("Store with localId $localId is marked as deleted locally.")
            entity?.toDomain()
                ?: throw NoSuchElementException("No store found with localId $localId.")
        }
    }

    override suspend fun saveStore(store: Store): Result<Unit> {
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
                isDeletedLocally = true, isSynced = false, updatedAt = System.currentTimeMillis()
            )
            storeDao.insertOrUpdate(storeToMarkAsDeleted)
            Result.success(Unit)
        }
    }
}