package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.data.local.dao.StoreDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.StoreDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.entity.User
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class StoreRepositoryImpl(
    private val storeDao: StoreDao,
    private val supabaseClient: SupabaseClient,
) : StoreRepository {

    override fun getStores(query: String): Flow<List<Store>> {
        return storeDao.searchStoresFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreByLocalId(id: Long): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreByLocalId(id)
            if (entity?.store?.isDeletedLocally == true) throw IllegalStateException("Store with localId $id is marked as deleted locally.")
            entity?.toDomain() ?: throw NoSuchElementException("No store found with localId ${id}.")
        }
    }

    override suspend fun getStoreBySeverId(id: Long): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreByServerId(id)
            if (entity?.store?.isDeletedLocally == true) throw IllegalStateException("Store with server id $id is marked as deleted locally.")
            entity?.toDomain() ?: throw NoSuchElementException("No store found with server id $id.")
        }
    }

    override suspend fun saveStore(store: Store): Result<Long> {
        return try {
            val entity = store.toDto()

            val result = if (store.id == Id.new) {
                supabaseClient.from("stores").insert(entity) {
                    select()
                }.decodeSingle<StoreDto>()
            } else {
                supabaseClient.from("stores").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<StoreDto>()
            }

            val localId = storeDao.insertOrUpdate(
                result.toEntity(store.employee.id.local).copy(localId = store.id.local)
            )

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteStore(store: Store): Result<Unit> {
        return runCatching {
            supabaseClient.from("stores").delete {
                filter {
                    eq("id", store.id.server!!)
                }
            }
            storeDao.deleteStoreByLocalId(store.id.local)
        }
    }

    override suspend fun syncWithServer(stores: List<StoreEntity>) {
        val entitiesToUpsert = stores.map { serverEntity ->
            val existingLocal = serverEntity.serverId?.let { storeDao.getStoreByServerId(it) }
            serverEntity.copy(
                localId = existingLocal?.store?.localId ?: 0L,
                createdAt = existingLocal?.store?.createdAt ?: serverEntity.createdAt
            )
        }
        storeDao.upsertAll(entitiesToUpsert)
    }

    override fun getStoresForUser(user: User): Flow<List<Store>> {
        return if (user.isAdmin) {
            getStores()
        } else {
            storeDao.getStoreByUserId(user.id.local).map {
                listOf(it.toDomain())
            }
        }
    }
}