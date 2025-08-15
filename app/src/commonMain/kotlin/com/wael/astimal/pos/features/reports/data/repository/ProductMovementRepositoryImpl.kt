package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import com.wael.astimal.pos.features.reports.domain.repository.ProductMovementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.atTime
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import kotlin.time.ExperimentalTime
import kotlin.time.toJavaInstant

@OptIn(ExperimentalTime::class)
class ProductMovementRepositoryImpl(
    private val db: AppDatabase,
) : ProductMovementRepository {

    override fun getProductMovement(
        startDate: LocalDate,
        endDate: LocalDate,
        productId: String?,
        storeId: String?,
    ): Flow<List<ProductMovementGroup>> {
        val startEpochMilli = startDate.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        val endEpochMilli =
            endDate.atTime(23, 59, 59).toInstant(TimeZone.UTC).toJavaInstant().toEpochMilli()

        return db.stockAdjustmentDao().getAll()
            .map { list -> list.map { it.toDomain() } }
            .map { allAdjustments ->
                // 1. Apply primary filters for store and product if they exist
                val filteredAdjustments = allAdjustments
                    .filter { storeId == null || it.store.id == storeId }
                    .filter { productId == null || it.product.id == productId }

                // 2. Group all filtered adjustments by the product ID
                val groupedByProduct = filteredAdjustments.groupBy { it.product.id }

                // 3. Process each product group individually
                groupedByProduct.mapNotNull { (_, adjustmentsForProduct) ->
                    val product = adjustmentsForProduct.first().product

                    // Filter transactions within the date range for this product
                    val adjustmentsInRange = adjustmentsForProduct
                        .filter { it.createdAt in startEpochMilli..endEpochMilli }
                        .sortedBy { it.createdAt }

                    val movementEntries = mutableListOf<ProductMovementEntry>()
                    var currentBalance = 0.0


                    // Create ledger entries with a running balance for this product
                    adjustmentsInRange.forEach { adjustment ->
                        currentBalance += adjustment.quantityChange
                        movementEntries.add(
                            ProductMovementEntry(
                                date = Instant.fromEpochMilliseconds(adjustment.createdAt)
                                    .toLocalDateTime(TimeZone.UTC).date,
                                productName = adjustment.product.name,
                                storeName = adjustment.store.name,
                                reason = adjustment.reason,
                                quantityIn = if (adjustment.quantityChange > 0) adjustment.quantityChange else 0.0,
                                quantityOut = if (adjustment.quantityChange < 0) -adjustment.quantityChange else 0.0,
                                balance = currentBalance
                            )
                        )
                    }
                    if (movementEntries.isNotEmpty())
                        ProductMovementGroup(
                            productName = product.name,
                            entries = movementEntries,
                            totalIn = movementEntries.sumOf { it.quantityIn },
                            totalOut = movementEntries.sumOf { it.quantityOut },
                            closingBalance = currentBalance
                        ) else null
                }
            }
    }
}