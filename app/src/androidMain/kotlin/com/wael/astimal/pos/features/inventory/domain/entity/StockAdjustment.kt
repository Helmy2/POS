package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.user.domain.entity.User

data class StockAdjustment(
    val store: Store,
    val product: Product,
    val user: User,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
) : Item

enum class StockAdjustmentReason {
    INITIAL_COUNT, RECOUNT, DAMAGED_GOODS, THEFT, OTHER;

    fun getStringResource(): Int {
        return when (this) {
            INITIAL_COUNT -> R.string.stock_adjustment_reason_initial_count
            RECOUNT -> R.string.stock_adjustment_reason_recount
            DAMAGED_GOODS -> R.string.stock_adjustment_reason_damaged_goods
            THEFT -> R.string.stock_adjustment_reason_theft
            OTHER -> R.string.stock_adjustment_reason_other
        }
    }
}

fun StockAdjustment.toEntity(): StockAdjustmentEntity {
    return StockAdjustmentEntity(
        localId = id.local,
        serverId = id.server,
        storeId = store.id.local,
        productId = product.id.local,
        userId = user.id,
        reason = reason,
        notes = notes,
        quantityChange = quantityChange,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}