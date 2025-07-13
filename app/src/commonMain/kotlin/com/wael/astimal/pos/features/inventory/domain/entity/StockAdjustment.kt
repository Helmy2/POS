package com.wael.astimal.pos.features.inventory.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.inventory.data.local.entity.StockAdjustmentEntity
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockAdjustmentDto
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.stock_adjustment_reason_damaged_goods
import pos.app.generated.resources.stock_adjustment_reason_initial_count
import pos.app.generated.resources.stock_adjustment_reason_invoice
import pos.app.generated.resources.stock_adjustment_reason_other
import pos.app.generated.resources.stock_adjustment_reason_recount
import pos.app.generated.resources.stock_adjustment_reason_theft

data class StockAdjustment(
    val store: Store,
    val product: Product,
    val user: User,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
    val invoice: Invoice?,
    override val id: Id,
    override val isSynced: Boolean = false,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
) : Item

enum class StockAdjustmentReason {
    OPENING_BALANCE, RECOUNT, DAMAGE, THEFT, OTHER, INVOICE;

    fun getStringResource(): StringResource {
        return when (this) {
            OPENING_BALANCE -> Res.string.stock_adjustment_reason_initial_count
            RECOUNT -> Res.string.stock_adjustment_reason_recount
            DAMAGE -> Res.string.stock_adjustment_reason_damaged_goods
            THEFT -> Res.string.stock_adjustment_reason_theft
            OTHER -> Res.string.stock_adjustment_reason_other
            INVOICE -> Res.string.stock_adjustment_reason_invoice
        }
    }
}

fun StockAdjustment.toEntity(): StockAdjustmentEntity {
    return StockAdjustmentEntity(
        localId = id.local,
        serverId = id.serverStringId,
        storeId = store.id.local,
        productId = product.id.local,
        userId = user.id.local,
        invoiceId = invoice?.id,
        reason = reason,
        notes = notes,
        quantityChange = quantityChange,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt
    )
}

fun StockAdjustment.toDto(): StockAdjustmentDto {
    return StockAdjustmentDto(
        id = id.serverStringId!!,
        storeId = store.id.server ?: 0,
        productId = product.id.server ?: 0,
        userId = user.id.serverStringId!!,
        reason = reason.name.lowercase(),
        notes = notes,
        quantity = quantityChange,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString()
    )
}