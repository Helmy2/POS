package com.wael.astimal.pos.features.inventory.data.entity


import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
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
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val storeId: Long,
    val productId: Long,
    val userId: Long,
    val reason: StockAdjustmentReason,
    val notes: String?,
    val quantityChange: Double,
    val date: Long,
    var isSynced: Boolean = false
)

data class StockAdjustmentWithDetails(
    @Embedded val adjustment: StockAdjustmentEntity,
    @Relation(
        parentColumn = "storeId",
        entityColumn = "localId"
    )
    val store: StoreEntity,
    @Relation(
        parentColumn = "productId",
        entityColumn = "localId",
        entity = ProductEntity::class
    )
    val productWithDetails: ProductWithDetailsEntity,
    @Relation(
        parentColumn = "userId",
        entityColumn = "id"
    )
    val user: UserEntity
)

fun StockAdjustmentWithDetails.toDomain(): StockAdjustment {
    return StockAdjustment(
        localId = this.adjustment.localId,
        serverId = this.adjustment.serverId,
        store = this.store.toDomain(),
        product = this.productWithDetails.toDomain(),
        user = this.user.toDomain(),
        reason = this.adjustment.reason,
        notes = this.adjustment.notes,
        quantityChange = this.adjustment.quantityChange,
        date = this.adjustment.date,
        isSynced = this.adjustment.isSynced
    )
}