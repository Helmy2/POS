package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.user.domain.entity.User

data class StockAdjustment(
    val localId: Long,
    val serverId: Int?,
    val store: Store,
    val product: Product,
    val user: User,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
    val date: Long,
    val isSynced: Boolean
)

enum class StockAdjustmentReason {
    INITIAL_COUNT,
    RECOUNT,
    DAMAGED_GOODS,
    THEFT,
    OTHER;

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