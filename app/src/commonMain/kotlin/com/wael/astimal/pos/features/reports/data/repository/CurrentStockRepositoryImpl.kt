package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import com.wael.astimal.pos.features.reports.domain.repository.CurrentStockRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class CurrentStockRepositoryImpl(
    private val db: AppDatabase
) : CurrentStockRepository {

    override fun getCurrentStock(
        productId: String?,
        storeId: String?
    ): Flow<List<CurrentStockInfo>> {
        // This Flow will re-calculate whenever stock adjustments change.
        return db.stockAdjustmentDao().getAll()
            .map { list -> list.map { it.toDomain() } }
            .map { allAdjustments ->
                // Apply the user's filters first
                val filteredAdjustments = allAdjustments
                    .filter { storeId == null || it.store.id == storeId }
                    .filter { productId == null || it.product.id == productId }

                // Group the adjustments by both product and store to get unique stock entries
                val groupedByProductAndStore = filteredAdjustments.groupBy {
                    Pair(it.product.id, it.store.id)
                }

                // Map each group to a CurrentStockInfo object with the summed quantity
                groupedByProductAndStore.map { (_, adjustments) ->
                    val first = adjustments.first()
                    val totalQuantity = adjustments.sumOf { it.quantityChange }

                    CurrentStockInfo(
                        product = first.product,
                        store = first.store,
                        quantity = totalQuantity
                    )
                }.filter { it.quantity != 0.0 }
            }
    }
}