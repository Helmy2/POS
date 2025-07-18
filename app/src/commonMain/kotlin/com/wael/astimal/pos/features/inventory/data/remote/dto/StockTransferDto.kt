package com.wael.astimal.pos.features.inventory.data.remote.dto

import StockTransferStatus
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StockTransferItemEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class StockTransferDto(
    val id: String,
    @SerialName("from_store_id") val fromStoreId: Long,
    @SerialName("to_store_id") val toStoreId: Long,
    @SerialName("initiating_user_id") val initiatingUserId: String,
    @SerialName("receiving_user_id") val receivingUserId: String,
    val notes: String?,
    val status: String,
    @SerialName("created_at") val createdAt: String,
    @SerialName("updated_at") val updatedAt: String,
)

@Serializable
data class StockTransferItemDto(
    val id: String,
    @SerialName("transfer_id") val transferId: String,
    @SerialName("product_id") val productId: Long,
    val quantity: Double,
)

@Serializable
data class NotificationDto(
    @SerialName("user_id") val userId: String,
    val message: String,
    @SerialName("related_transfer_id") val relatedTransferId: String
)

fun StockTransferDto.toEntity(
    fromStoreId: Long,
    toStoreId: Long,
    initiatingUserId: Long,
    receivingUserId: Long
): StockTransferEntity {
    return StockTransferEntity(
        localId = id,
        fromStoreId = fromStoreId,
        toStoreId = toStoreId,
        initiatedByUserId = initiatingUserId,
        receivingUserId = receivingUserId,
        notes = notes,
        status = StockTransferStatus.valueOf(status.uppercase()),
        isSynced = true,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now()
    )
}

fun StockTransferItemDto.toEntity(
    productLocalId: Long,
): StockTransferItemEntity {
    return StockTransferItemEntity(
        localId = id,
        stockTransferLocalId = transferId,
        productLocalId = productLocalId,
        quantity = quantity,
    )
}