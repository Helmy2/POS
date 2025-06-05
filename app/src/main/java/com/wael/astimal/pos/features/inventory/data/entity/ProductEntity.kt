package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.ColumnInfo
import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.Product

@Entity(
    tableName = "products", foreignKeys = [ForeignKey(
        entity = CategoryEntity::class,
        parentColumns = ["localId"],
        childColumns = ["categoryId"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = StoreEntity::class,
        parentColumns = ["localId"],
        childColumns = ["storeId"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = UnitEntity::class,
        parentColumns = ["localId"],
        childColumns = ["minimumStockUnitId"],
        onDelete = ForeignKey.SET_NULL
    ), ForeignKey(
        entity = UnitEntity::class,
        parentColumns = ["localId"],
        childColumns = ["maximumStockUnitId"],
        onDelete = ForeignKey.SET_NULL
    )]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val arName: String?,
    val enName: String?,

    @ColumnInfo(index = true) val categoryId: Long?,

    val averagePrice: Double?,
    val sellingPrice: Double?,

    val openingBalanceQuantity: Double?,
    @ColumnInfo(index = true) val storeId: Long?,

    val minimumStockLevel: Int?,
    @ColumnInfo(index = true) val minimumStockUnitId: Long?,
    val maximumStockLevel: Int?,
    @ColumnInfo(index = true) val maximumStockUnitId: Long?,
    val firstPeriodData: String?,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

data class ProductWithDetailsEntity(
    @Embedded val product: ProductEntity,

    @Relation(
        parentColumn = "categoryId", entityColumn = "localId", entity = CategoryEntity::class
    ) val category: CategoryEntity?,

    @Relation(
        parentColumn = "storeId", entityColumn = "localId", entity = StoreEntity::class
    ) val store: StoreEntity?,


    @Relation(
        parentColumn = "minimumStockUnitId", entityColumn = "localId", entity = UnitEntity::class
    ) val minimumStockUnit: UnitEntity?,

    @Relation(
        parentColumn = "maximumStockUnitId", entityColumn = "localId", entity = UnitEntity::class
    ) val maximumStockUnit: UnitEntity?
)


fun ProductWithDetailsEntity.toDomain(): Product {
    return Product(
        localId = product.localId,
        serverId = product.serverId,
        localizedName = LocalizedString(
            arName = product.arName,
            enName = product.enName
        ),
        averagePrice = product.averagePrice,
        sellingPrice = product.sellingPrice,
        openingBalanceQuantity = product.openingBalanceQuantity,
        minimumStockLevel = product.minimumStockLevel,
        maximumStockLevel = product.maximumStockLevel,
        firstPeriodData = product.firstPeriodData,
        isSynced = product.isSynced,
        lastModified = product.lastModified,
        isDeletedLocally = product.isDeletedLocally,
        category = category?.toDomain(),
        store = store?.toDomain(),
        minimumUnit = minimumStockUnit?.toDomain(),
        maximumUnit = maximumStockUnit?.toDomain(),
    )
}
