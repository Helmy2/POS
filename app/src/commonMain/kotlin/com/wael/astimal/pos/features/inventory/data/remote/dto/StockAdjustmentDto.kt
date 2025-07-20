package com.wael.astimal.pos.features.inventory.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockAdjustmentDto(
    val id: String,
    @SerialName("store_id")
    val storeId: String,
    @SerialName("product_id")
    val productId: String,
    @SerialName("user_id")
    val userId: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    val reason: String,
    val notes: String?,
    val quantity: Double,
    @SerialName("invoice_id")
    val invoiceId: String?
)

fun StockAdjustmentDto.toEntity(): StockAdjustmentEntity {
    return StockAdjustmentEntity(
        localId = id,
        storeId = storeId,
        productId = productId,
        userId = userId,
        reason = StockAdjustmentReason.valueOf(reason.uppercase()),
        notes = notes,
        quantityChange = quantity,
        isSynced = true,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        invoiceId = invoiceId
    )
}