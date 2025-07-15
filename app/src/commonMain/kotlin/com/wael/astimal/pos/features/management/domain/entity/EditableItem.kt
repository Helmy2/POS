package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import java.util.UUID

data class EditableItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val isSelectedUnitIsMax: Boolean = true,
    val subUnitQuantity: String = "1",
    val mainUnitQuantity: String = "1",
    val subUnitPrice: String = "0",
    val mainUnitPrice: String = "0",
    val currentStock: Double = 0.0
) {
    val lineTotal: Double
        get() {
            val quantity = mainUnitQuantity.toDoubleOrNull() ?: 0.0
            val price = mainUnitPrice.toDoubleOrNull() ?: 0.0
            return quantity * price
        }
}