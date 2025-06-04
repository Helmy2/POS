package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem

@Entity(
    tableName = "stock_transfers",
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["fromStoreId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["toStoreId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["initiatedByUserId"],
            onDelete = ForeignKey.SET_NULL
        )
    ]
)
data class StockTransferEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?, // Server ID for the transfer header
    @ColumnInfo(index = true)
    val fromStoreId: Long?,
    @ColumnInfo(index = true)
    val toStoreId: Long?,
    @ColumnInfo(index = true)
    val initiatedByUserId: Long?, // User who created the transfer
    var isAccepted: Boolean?, // null = pending, true = accepted, false = rejected/cancelled
    val transferDate: Long, // Timestamp of creation/initiation

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "stock_transfer_items",
    foreignKeys = [
        ForeignKey(
            entity = StockTransferEntity::class,
            parentColumns = ["localId"],
            childColumns = ["stockTransferLocalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productLocalId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["unitLocalId"],
            onDelete = ForeignKey.RESTRICT
        )
    ]
)
data class StockTransferItemEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    @ColumnInfo(index = true)
    val stockTransferLocalId: Long,
    @ColumnInfo(index = true)
    val productLocalId: Long,
    @ColumnInfo(index = true)
    val unitLocalId: Long,
    val quantity: Double,
    val maximumOpeningBalance: Double?,
    val minimumOpeningBalance: Double?
)

data class StockTransferWithItemsAndDetails(
    @Embedded
    val transfer: StockTransferEntity,

    @Relation(
        parentColumn = "fromStoreId",
        entityColumn = "localId",
        entity = StoreEntity::class
    )
    val fromStore: StoreEntity?,

    @Relation(
        parentColumn = "toStoreId",
        entityColumn = "localId",
        entity = StoreEntity::class
    )
    val toStore: StoreEntity?,

    @Relation(
        parentColumn = "initiatedByUserId",
        entityColumn = "id", // Corrected: UserEntity's PK is 'id'
        entity = UserEntity::class
    )
    val initiatedByUser: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "stockTransferLocalId",
        entity = StockTransferItemEntity::class
    )
    val itemsWithProducts: List<StockTransferItemWithProductDetails>
)

data class StockTransferItemWithProductDetails(
    @Embedded
    val item: StockTransferItemEntity,

    @Relation(
        parentColumn = "productLocalId",
        entityColumn = "localId",
        entity = ProductEntity::class
    )
    val product: ProductEntity?,

    @Relation(
        parentColumn = "unitLocalId",
        entityColumn = "localId",
        entity = UnitEntity::class
    )
    val unit: UnitEntity?
)


fun StockTransferWithItemsAndDetails.toDomain(): StockTransfer {
    val fromStoreNameDisplay = "${fromStore?.enName}: ${fromStore?.arName}"
    val toStoreNameDisplay = "${toStore?.enName}: ${toStore?.arName}"
    // todo
    return StockTransfer(
        localId = this.transfer.localId,
        serverId = this.transfer.serverId,
        fromStoreId = this.transfer.fromStoreId ?: 0L,
        fromStoreName = fromStoreNameDisplay,
        toStoreId = this.transfer.toStoreId ?: 0L,
        toStoreName = toStoreNameDisplay,
        initiatedByUserId = this.transfer.initiatedByUserId ?: 0L,
        initiatedByUserName = "todo" ,
        isAccepted = this.transfer.isAccepted,
        transferDate = this.transfer.transferDate,
        items = this.itemsWithProducts.map { it.toDomain() },
        isSynced = this.transfer.isSynced,
        lastModified = this.transfer.lastModified,
        isDeletedLocally = this.transfer.isDeletedLocally
    )
}

fun StockTransferItemWithProductDetails.toDomain(): StockTransferItem {

    return StockTransferItem(
        localId = this.item.localId,
        serverId = this.item.serverId,
        productLocalId = this.item.productLocalId,
        productArName = this.product?.arName,
        productEnName = this.product?.enName,
        unitLocalId = this.item.unitLocalId,
        unitArName = this.unit?.arName,
        unitEnName =  this.unit?.enName,
        quantity = this.item.quantity,
        maximumOpeningBalance = this.item.maximumOpeningBalance,
        minimumOpeningBalance = this.item.minimumOpeningBalance
    )
}