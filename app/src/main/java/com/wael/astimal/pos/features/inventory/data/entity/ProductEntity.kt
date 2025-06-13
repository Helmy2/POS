package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.Product

@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["localId"],
            childColumns = ["categoryId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["minimumUnitId"],
            onDelete = ForeignKey.SET_NULL
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["maximumUnitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("categoryId"),
        Index("storeId"),
        Index("minimumUnitId"),
        Index("maximumUnitId")
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0,
    val serverId: Int?,
    val arName: String,
    val enName: String,
    val categoryId: Long?,
    val storeId: Long?,
    val openingBalanceQuantity: Double? = null,
    val averagePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val minimumUnitId: Long?,
    val maximumUnitId: Long?,
    val subUnitsPerMainUnit: Double,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

data class ProductWithDetailsEntity(
    @Embedded
    val product: ProductEntity,
    @Relation(
        parentColumn = "categoryId",
        entityColumn = "localId"
    )
    val category: CategoryEntity?,
    @Relation(
        parentColumn = "storeId",
        entityColumn = "localId"
    )
    val store: StoreEntity?,
    @Relation(
        parentColumn = "minimumUnitId",
        entityColumn = "localId"
    )
    val minimumUnit: UnitEntity?,
    @Relation(
        parentColumn = "maximumUnitId",
        entityColumn = "localId"
    )
    val maximumUnit: UnitEntity?
)

fun ProductWithDetailsEntity.toDomain(): Product {
    return Product(
        localId = this.product.localId,
        serverId = this.product.serverId,
        localizedName = LocalizedString(
            arName = this.product.arName,
            enName = this.product.enName
        ),
        category = this.category?.toDomain(),
        store = this.store?.toDomain(),
        averagePrice = this.product.averagePrice,
        sellingPrice = this.product.sellingPrice,
        minimumProductUnit = this.minimumUnit?.toDomain(),
        maximumProductUnit = this.maximumUnit?.toDomain()
            ?: throw IllegalStateException("Maximum unit cannot be null"),
        subUnitsPerMainUnit = this.product.subUnitsPerMainUnit,
        isSynced = this.product.isSynced,
        openingBalanceQuantity = this.product.openingBalanceQuantity,
        lastModified = this.product.lastModified,
    )
}
