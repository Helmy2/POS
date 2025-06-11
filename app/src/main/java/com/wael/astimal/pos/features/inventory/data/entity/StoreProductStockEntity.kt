package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.ForeignKey

@Entity(
    tableName = "store_product_stock",
    primaryKeys = ["storeLocalId", "productLocalId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeLocalId"],
            onDelete = ForeignKey.CASCADE
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productLocalId"],
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class StoreProductStockEntity(
    @ColumnInfo(index = true)
    val storeLocalId: Long,
    @ColumnInfo(index = true)
    val productLocalId: Long,
    val quantity: Double
)