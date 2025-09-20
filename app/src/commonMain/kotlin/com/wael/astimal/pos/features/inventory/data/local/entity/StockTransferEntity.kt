package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.remote.dto.StockTransferItemDto
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransfer
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferItem
import com.wael.astimal.pos.features.inventory.domain.entity.StockTransferStatus
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain


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
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["receivingUserId"],
        ),
    ]
)
data class StockTransferEntity(
    @PrimaryKey val localId: String,
    var isSynced: Boolean = false,
    val createdAt: Long = Clock.now(),
    val updatedAt: Long = Clock.now(),
    val transferDate: Long = Clock.now(),
    var isDeletedLocally: Boolean = false,

    @ColumnInfo(index = true) val fromStoreId: String?,
    @ColumnInfo(index = true) val toStoreId: String?,
    @ColumnInfo(index = true) val initiatedByUserId: String?,
    @ColumnInfo(index = true) val receivingUserId: String?,
    val notes: String?,
    val status: StockTransferStatus,
)

@Entity(
    tableName = "stock_transfer_items", foreignKeys = [ForeignKey(
        entity = StockTransferEntity::class,
        parentColumns = ["localId"],
        childColumns = ["stockTransferLocalId"],
    ), ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["localId"],
        childColumns = ["productLocalId"],
    )]
)
data class StockTransferItemEntity(
    @PrimaryKey val localId: String,
    @ColumnInfo(index = true) val stockTransferLocalId: String,
    @ColumnInfo(index = true) val productLocalId: String,
    val quantity: Double,
    val isDeletedLocally: Boolean = false,
    val isSynced: Boolean = false
)

data class StockTransferWithItemsAndDetails(
    @Embedded val transfer: StockTransferEntity,

    @Relation(
        parentColumn = "fromStoreId", entityColumn = "localId", entity = StoreEntity::class
    ) val fromStore: StoreWithDetails?,

    @Relation(
        parentColumn = "toStoreId", entityColumn = "localId", entity = StoreEntity::class
    ) val toStore: StoreWithDetails?,

    @Relation(
        parentColumn = "initiatedByUserId", entityColumn = "id", entity = UserEntity::class
    ) val initiatedByUser: UserEntity?,

    @Relation(
        parentColumn = "receivingUserId", entityColumn = "id", entity = UserEntity::class
    ) val receivingUser: UserEntity?,

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
        id = transfer.localId,
        fromStore = fromStore?.toDomain() ?: throw NullPointerException(),
        toStore = toStore?.toDomain() ?: throw NullPointerException(),
        initiatingUser = initiatedByUser?.toDomain() ?: throw NullPointerException(),
        items = itemsWithProducts.map { it.toDomain() },
        isSynced = transfer.isSynced,
        updatedAt = transfer.updatedAt,
        createdAt = transfer.createdAt,
        notes = transfer.notes,
        status = transfer.status,
        receivingUser = receivingUser?.toDomain() ?: throw NullPointerException(),
        transferDate = transfer.transferDate
    )
}

fun StockTransferItemWithProductDetails.toDomain(): StockTransferItem {
    return StockTransferItem(
        id = item.localId,
        quantity = item.quantity,
        product = product?.toDomain() ?: throw NullPointerException(),
    )
}

fun StockTransferItemEntity.toDto(): StockTransferItemDto {
    return StockTransferItemDto(
        id = localId,
        quantity = quantity,
        transferId = stockTransferLocalId,
        productId = productLocalId,
    )
}