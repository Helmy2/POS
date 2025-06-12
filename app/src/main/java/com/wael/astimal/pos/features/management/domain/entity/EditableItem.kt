package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.ProductUnit
import java.util.UUID

data class EditableItem(
    val tempEditorId: String = UUID.randomUUID().toString(),
    val product: Product? = null,
    val selectedProductUnit: ProductUnit? = null,
    val quantity: String = "1",
    val price: String = "0.0",
) {
    val lineTotal: Double
        get() {
            val quantity = this.quantity.toDoubleOrNull() ?: 0.0
            val price = this.price.toDoubleOrNull() ?: 0.0
            return quantity * price
        }
}