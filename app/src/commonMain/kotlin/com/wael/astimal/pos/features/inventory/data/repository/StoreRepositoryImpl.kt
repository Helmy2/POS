package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.util.deleteRecordAndLog
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
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class StoreRepositoryImpl(
    private val storeDao: StoreDao,
    private val supabaseClient: SupabaseClient,
) : StoreRepository {

    override fun getStores(query: String): Flow<List<Store>> {
        return storeDao.searchStoresFlow(query).map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override suspend fun getStoreById(id: String): Result<Store> {
        return runCatching {
            val entity = storeDao.getStoreById(id)
            if (entity?.store?.isDeletedLocally == true) throw IllegalStateException("Store with localId $id is marked as deleted locally.")
            entity?.toDomain() ?: throw NoSuchElementException("No store found with localId ${id}.")
        }
    }


    @OptIn(ExperimentalUuidApi::class)
    override suspend fun saveStore(store: Store): Result<Unit> {
        return try {
            val entity = store.toDto()

            val result = if (store.id == "") {
                supabaseClient.from("stores").insert(
                    entity.copy(id = Uuid.random().toString())
                ) {
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

            val localId = storeDao.upsert(result.toEntity())

            Result.success(localId)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteStore(store: Store): Result<Unit> {
        return try {
            supabaseClient.deleteRecordAndLog(
                targetTableName = "stores",
                targetRecordId = store.id
            )
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(stores: List<StoreEntity>): Result<Unit> {
        return runCatching {
            storeDao.upsertAll(stores)
        }
    }

    override fun getStoresForUser(user: User): Flow<List<Store>> {
        return storeDao.getStoreByUserId(user.id).map {
            listOfNotNull(it?.toDomain())
        }
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { storeDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}