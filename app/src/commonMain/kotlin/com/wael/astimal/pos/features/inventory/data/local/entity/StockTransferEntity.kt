package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity

@Entity(
    tableName = "stock_transfers",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["fromStoreId"],
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["toStoreId"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["initiatedByUserId"],
        )
    ]
)
data class StockTransferEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,

    @ColumnInfo(index = true) val fromStoreId: Long?,
    @ColumnInfo(index = true) val toStoreId: Long?,
    @ColumnInfo(index = true) val initiatedByUserId: Long?,
) : ItemEntity

@Entity(
    tableName = "stock_transfer_items", foreignKeys = [ForeignKey(
        entity = StockTransferEntity::class,
        parentColumns = ["localId"],
        childColumns = ["stockTransferLocalId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["localId"],
        childColumns = ["productLocalId"],
        onDelete = ForeignKey.RESTRICT
    )]
)
data class StockTransferItemEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Long?,
    @ColumnInfo(index = true) val stockTransferLocalId: Long,
    @ColumnInfo(index = true) val productLocalId: Long,
    val quantity: Double,
)

data class StockTransferWithItemsAndDetails(
    @Embedded val transfer: StockTransferEntity,

    @Relation(
        parentColumn = "fromStoreId", entityColumn = "localId", entity = StoreEntity::class
    ) val fromStore: StoreEntity?,

    @Relation(
        parentColumn = "toStoreId", entityColumn = "localId", entity = StoreEntity::class
    ) val toStore: StoreEntity?,

    @Relation(
        parentColumn = "initiatedByUserId", entityColumn = "id", entity = UserEntity::class
    ) val initiatedByUser: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "stockTransferLocalId",
        entity = StockTransferItemEntity::class
    ) val itemsWithProducts: List<StockTransferItemWithProductDetails>
)

data class StockTransferItemWithProductDetails(
    @Embedded val item: StockTransferItemEntity,

    @Relation(
        parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class
    ) val product: ProductWithDetails?,
)


fun StockTransferWithItemsAndDetails.toDomain(): StockTransfer {
    return StockTransfer(
        id = Id(transfer.localId, transfer.serverId),
        fromStore = fromStore?.toDomain() ?: throw NullPointerException(),
        toStore = toStore?.toDomain() ?: throw NullPointerException(),
        initiatedByUser = initiatedByUser ?: throw NullPointerException(),
        items = itemsWithProducts.map { it.toDomain() },
        isSynced = transfer.isSynced,
        updatedAt = transfer.updatedAt,
        createdAt = transfer.createdAt
    )
}

fun StockTransferItemWithProductDetails.toDomain(): StockTransferItem {
    return StockTransferItem(
        id = Id(item.localId, item.serverId),
        quantity = item.quantity,
        product = product?.toDomain() ?: throw NullPointerException(),
    )
}