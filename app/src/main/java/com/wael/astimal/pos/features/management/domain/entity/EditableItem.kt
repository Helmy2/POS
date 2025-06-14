package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import java.util.UUID

data class EditableItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val isSelectedUnitIsMax: Boolean = true,
    val minUnitQuantity: String = "",
    val maxUnitQuantity: String = "",
    val minUnitPrice: String = "0",
    val maxUnitPrice: String = "0",
    val currentStock: Double = 0.0
) {
    val lineTotal: Double
        get() {
            val quantity = maxUnitQuantity.toDoubleOrNull() ?: 0.0
            val price = maxUnitPrice.toDoubleOrNull() ?: 0.0
            return quantity * price
        }
}