package com.wael.astimal.pos.features.inventory.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product


@Entity(
    tableName = "products",
    foreignKeys = [
        ForeignKey(
            entity = CategoryEntity::class,
            parentColumns = ["localId"],
            childColumns = ["categoryId"],
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeId"],
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["minimumUnitId"],
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["maximumUnitId"],
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
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,


    val arName: String,
    val enName: String,
    val categoryId: Long?,
    val storeId: Long?,
    val openingBalanceQuantity: Double,
    val averagePrice: Double = 0.0,
    val sellingPrice: Double = 0.0,
    val minimumUnitId: Long?,
    val maximumUnitId: Long?,
    val subUnitsPerMainUnit: Double,
) : ItemEntity

data class ProductWithDetailsEntity(
    @Embedded val product: ProductEntity,
    @Relation(
        parentColumn = "categoryId", entityColumn = "localId"
    ) val category: CategoryEntity?,
    @Relation(
        parentColumn = "storeId", entityColumn = "localId"
    ) val store: StoreEntity?,
    @Relation(
        parentColumn = "minimumUnitId", entityColumn = "localId"
    ) val minimumUnit: UnitEntity?,
    @Relation(
        parentColumn = "maximumUnitId", entityColumn = "localId"
    ) val maximumUnit: UnitEntity?,
)

fun ProductWithDetailsEntity.toDomain(): Product {
    return Product(
        name = LocalizedString(
            arName = product.arName, enName = product.enName
        ),
        category = category?.toDomain() ?: throw NullPointerException(),
        store = store?.toDomain() ?: throw NullPointerException(),
        averagePrice = product.averagePrice,
        sellingPrice = product.sellingPrice,
        minimumProductUnit = minimumUnit?.toDomain(),
        maximumProductUnit = maximumUnit?.toDomain() ?: throw NullPointerException(),
        subUnitsPerMainUnit = product.subUnitsPerMainUnit,
        isSynced = product.isSynced,
        openingBalanceQuantity = product.openingBalanceQuantity,
        updatedAt = product.updatedAt,
        createdAt = product.createdAt,
        id = Id(product.localId, product.serverId)
    )
}
