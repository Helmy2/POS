package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.data.SyncManager
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.deleteRecordAndLog
import com.wael.astimal.pos.core.util.pushAll
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockAdjustmentDto
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.entity.toEntity
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.firstOrNull
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import kotlin.uuid.ExperimentalUuidApi
import kotlin.uuid.Uuid


class StockRepositoryImpl(
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val supabaseClient: SupabaseClient,
    private val userRepository: UserRepository,
    private val storeRepository: StoreRepository,
    private val syncManager: SyncManager
) : StockRepository {

    override fun getStoreStocks(
        query: String, selectedStoreId: String?
    ): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAll().map { entities ->
            entities.map { it.toDomain() }.filter {
                it.reason != StockAdjustmentReason.INVOICE
            }
        }
    }

    override fun getStockQuantity(storeId: String, productId: String): Flow<Double> {
        return stockAdjustmentDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun getStockQuantity(productId: String): Double {
        return stockAdjustmentDao.getStockTotalQuantity(productId) ?: 0.0
    }

    override suspend fun getStockInCurrentStore(productId: String): Double {
        return withContext(Dispatchers.IO) {
            val user = userRepository.getCurrentUser() ?: return@withContext 0.0
            val stores =
                storeRepository.getStoresForUser(user).firstOrNull() ?: return@withContext 0.0

            return@withContext stockAdjustmentDao.getStockInStores(stores.map { it.id }, productId)
                ?: 0.0
        }
    }

    @OptIn(ExperimentalUuidApi::class)
    override suspend fun addStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return try {

            val adjustmentToInsert = if (adjustment.id == "") adjustment
                .copy(id = Uuid.random().toString())
            else adjustment

            supabaseClient.pushAll<StockAdjustmentDto>("stock_adjustments") {
                listOf(adjustmentToInsert.toDto(Clock.now().toISOString()))
            }

            stockAdjustmentDao.insert(adjustmentToInsert.toEntity())
            syncManager.restSyncDate()
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return try {
            supabaseClient.deleteRecordAndLog(
                targetTableName = "stock_adjustments",
                targetRecordId = adjustment.id
            ).onSuccess {
                syncManager.restSyncDate()
            }
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun syncWithServer(adjustments: List<StockAdjustmentEntity>): Result<Unit> {
        return runCatching {
            stockAdjustmentDao.upsertAll(adjustments)
        }
    }

    override suspend fun getAllUnSynced(): Result<List<StockAdjustment>> {
        return try {
            Result.success(stockAdjustmentDao.getAllUnSynced().map { it.toDomain() })
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }

    override suspend fun deleteAll(ids: List<String>): Result<Unit> {
        return try {
            ids.forEach { stockAdjustmentDao.hardDelete(it) }
            Result.success(Unit)
        } catch (e: Exception) {
            e.printStackTrace()
            Result.failure(e)
        }
    }
}