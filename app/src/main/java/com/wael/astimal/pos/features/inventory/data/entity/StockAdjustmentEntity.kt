package com.wael.astimal.pos.features.inventory.data.entity


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustment
import com.wael.astimal.pos.features.inventory.domain.entity.StockAdjustmentReason
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "stock_adjustments",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class StockAdjustmentEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,

    val storeId: Long,
    val productId: Long,
    val userId: Long,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
) : ItemEntity

data class StockAdjustmentWithDetails(
    @Embedded val adjustment: StockAdjustmentEntity, @Relation(
        parentColumn = "storeId", entityColumn = "localId"
    ) val store: StoreEntity?, @Relation(
        parentColumn = "productId", entityColumn = "localId", entity = ProductEntity::class
    ) val productWithDetails: ProductWithDetailsEntity?, @Relation(
        parentColumn = "userId", entityColumn = "id"
    ) val user: UserEntity?
)

fun StockAdjustmentWithDetails.toDomain(): StockAdjustment {
    return StockAdjustment(
        store = store?.toDomain() ?: throw NullPointerException(),
        product = productWithDetails?.toDomain() ?: throw NullPointerException(),
        user = user?.toDomain() ?: throw NullPointerException(),
        reason = adjustment.reason,
        notes = adjustment.notes,
        quantityChange = adjustment.quantityChange,
        id = Id(adjustment.localId, adjustment.serverId),
        isSynced = adjustment.isSynced,
        updatedAt = adjustment.updatedAt,
        createdAt = adjustment.createdAt
    )
}