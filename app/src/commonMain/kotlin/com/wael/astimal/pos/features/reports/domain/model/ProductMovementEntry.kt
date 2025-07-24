package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import kotlinx.datetime.LocalDate

/**
 * Represents a single row in the product movement ledger.
 */
data class ProductMovementEntry(
    val date: LocalDate,
    val productName: LocalizedString,
    val storeName: LocalizedString,
    val reason: StockAdjustmentReason,
    val quantityIn: Double,
    val quantityOut: Double,
    val balance: Double,
)

data class ProductMovementGroup(
    val productName: LocalizedString,
    val entries: List<ProductMovementEntry>,
    val totalIn: Double,
    val totalOut: Double,
    val closingBalance: Double,
)