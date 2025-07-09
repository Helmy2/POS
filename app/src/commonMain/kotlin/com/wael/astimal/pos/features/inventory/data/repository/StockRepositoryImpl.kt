package com.wael.astimal.pos.features.inventory.data.repository

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.data.local.dao.StockAdjustmentDao
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockAdjustmentDto
import com.wael.astimal.pos.features.inventory.data.remote.dto.toEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.toDto
import com.wael.astimal.pos.features.inventory.domain.repository.ProductRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StockRepository
import com.wael.astimal.pos.features.inventory.domain.repository.StoreRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.postgrest.from
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map


class StockRepositoryImpl(
    private val stockAdjustmentDao: StockAdjustmentDao,
    private val supabaseClient: SupabaseClient,
    private val storeRepository: StoreRepository,
    private val productRepository: ProductRepository,
    private val userRepository: UserRepository
) : StockRepository {

    override fun getStoreStocks(
        query: String,
        selectedStoreId: Long?
    ): Flow<List<StockAdjustment>> {
        return stockAdjustmentDao.getAll().map { entities ->
            entities.map { it.toDomain() }
        }
    }

    override fun getStockQuantityFlow(storeId: Long, productId: Long): Flow<Double> {
        return stockAdjustmentDao.getStockQuantity(storeId, productId).map { it ?: 0.0 }
    }

    override suspend fun addStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return runCatching {
            val entity = adjustment.toDto()

            val result = if (adjustment.id == Id.new) {
                supabaseClient.from("stock_adjustments").insert(entity) {
                    select()
                }.decodeSingle<StockAdjustmentDto>()
            } else {
                supabaseClient.from("stock_adjustments").update(entity) {
                    filter {
                        eq("id", entity.id)
                    }
                    select()
                }.decodeSingle<StockAdjustmentDto>()
            }

            stockAdjustmentDao.insert(
                result.toEntity(
                    storeId = storeRepository.getStoreBySeverId(
                        result.storeId
                    ).getOrThrow().id.local,
                    productId = productRepository.getProductByServerId(
                        result.productId
                    ).getOrThrow().id.local,
                    userId = userRepository.getUserByServerId(
                        result.userId
                    ).getOrThrow()!!.id.local
                ).copy(localId = adjustment.id.local)
            )
        }
    }

    override suspend fun deleteStockAdjustment(adjustment: StockAdjustment): Result<Unit> {
        return runCatching {
            supabaseClient.from("stock_adjustments").delete {
                filter {
                    eq("id", adjustment.id.server!!)
                }
                select()
            }.decodeSingle<StockAdjustmentDto>()

            stockAdjustmentDao.deleteByLocalId(adjustment.id.local)
        }
    }

    override suspend fun syncWithServer(adjustments: List<StockAdjustmentEntity>): Result<Unit> {
        return runCatching {
            adjustments.map {
                val existingEntity = stockAdjustmentDao.getAdjustmentByServerId(
                    it.serverId ?: throw Exception("Server ID not found")
                )
                it.copy(localId = existingEntity?.localId ?: 0L)
            }.also {
                stockAdjustmentDao.upsertAll(it)
            }
        }
    }
}