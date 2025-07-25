package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store

/**
 * Represents a snapshot of the current stock quantity for a specific product in a specific store.
 */
data class CurrentStockInfo(
    val product: Product,
    val store: Store,
    val quantity: Double
)