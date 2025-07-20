package com.wael.astimal.pos.features.inventory.data.local.entity


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceWithItems
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain

@Entity(
    tableName = "stock_adjustments",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeId"],
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productId"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
        ),
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["supabaseId"],
            childColumns = ["invoiceId"],
        ),
        ForeignKey(
            entity = StockTransferEntity::class,
            parentColumns = ["localId"],
            childColumns = ["transactionId"],
        ),
    ]
)
data class StockAdjustmentEntity(
    @PrimaryKey val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,

    val storeId: String,
    val productId: String,
    val invoiceId: String? = null,
    val transactionId: String? = null,
    val userId: String,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
)

data class StockAdjustmentWithDetails(
    @Embedded val adjustment: StockAdjustmentEntity,

    @Relation(
        parentColumn = "storeId", entityColumn = "localId", entity = StoreEntity::class
    ) val store: StoreWithDetails?,

    @Relation(
        parentColumn = "productId", entityColumn = "localId", entity = ProductEntity::class
    ) val productWithDetails: ProductWithDetails?,

    @Relation(
        parentColumn = "userId", entityColumn = "id"
    ) val user: UserEntity?,

    @Relation(
        parentColumn = "invoiceId", entityColumn = "supabaseId", entity = InvoiceEntity::class
    ) val invoice: InvoiceWithItems?
)

fun StockAdjustmentWithDetails.toDomain(): StockAdjustment {
    return StockAdjustment(
        store = store?.toDomain() ?: throw NullPointerException(),
        product = productWithDetails?.toDomain() ?: throw NullPointerException(),
        user = user?.toDomain() ?: throw NullPointerException(),
        reason = adjustment.reason,
        notes = adjustment.notes,
        quantityChange = adjustment.quantityChange,
        id = adjustment.localId,
        isSynced = adjustment.isSynced,
        updatedAt = adjustment.updatedAt,
        createdAt = adjustment.createdAt,
        invoice = invoice?.toDomain()
    )
}