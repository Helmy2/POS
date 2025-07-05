package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.domain.entity.StoreStock

@Entity(
    tableName = "store_product_stock",
    primaryKeys = ["storeLocalId", "productLocalId"],
    foreignKeys = [
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeLocalId"],
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productLocalId"],
        )
    ],
    indices = [
        Index(value = ["storeLocalId"]),
        Index(value = ["productLocalId"])
    ]
)
data class StoreProductStockEntity(
    val storeLocalId: Long,
    val productLocalId: Long,
    val quantity: Double
)

data class StoreStockWithDetails(
    @Embedded
    val stock: StoreProductStockEntity,

    @Relation(
        parentColumn = "storeLocalId",
        entityColumn = "localId"
    )
    val store: StoreEntity?,

    @Relation(
        parentColumn = "productLocalId",
        entityColumn = "localId",
        entity = ProductEntity::class
    )
    val productWithDetails: ProductWithDetails?
)

fun StoreStockWithDetails.toDomain(): StoreStock {
    return StoreStock(
        store = store?.toDomain() ?: throw NullPointerException(),
        product = productWithDetails?.toDomain() ?: throw NullPointerException(),
        quantity = stock.quantity
    )
}