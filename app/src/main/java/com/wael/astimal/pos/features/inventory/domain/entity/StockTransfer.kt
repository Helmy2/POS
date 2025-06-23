package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.inventory.data.entity.StockTransferItemEntity
import com.wael.astimal.pos.features.user.data.entity.UserEntity

data class StockTransfer(
    val fromStore: Store,
    val toStore: Store,
    val initiatedByUser: UserEntity,
    val items: List<StockTransferItem>,
    override val id: Id,
    override val isSynced: Boolean,
    override val createdAt: Long,
    override val updatedAt: Long
) : Item

data class StockTransferItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
)

fun StockTransferItem.toEntity(
    stockTransferLocalId: Long
): StockTransferItemEntity {
    return StockTransferItemEntity(
        localId = id.local,
        serverId = id.server,
        productLocalId = product.id.local,
        quantity = quantity,
        stockTransferLocalId = stockTransferLocalId
    )
}

