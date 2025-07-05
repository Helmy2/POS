package com.wael.astimal.pos.features.inventory.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.inventory.domain.entity.Product

/**
 * Represents a product in the local Room database.
 * This entity is designed to be the offline source of truth for product information.
 */
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
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["mainUnitId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UnitEntity::class,
            parentColumns = ["localId"],
            childColumns = ["subUnitId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index("serverId", unique = true),
        Index("categoryId"),
        Index("mainUnitId"),
        Index("subUnitId"),
    ]
)
data class ProductEntity(
    @PrimaryKey(autoGenerate = true)
    override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long,
    override var updatedAt: Long,
    override var isDeletedLocally: Boolean = false,

    val arName: String?,
    val enName: String,
    val barcode: String?,
    val purchasePrice: Double,
    val sellingPrice: Double,
    val averagePurchasePrice: Double,
    val categoryId: Long?,
    val mainUnitId: Long,
    val subUnitId: Long?,
    val subUnitsPerMainUnit: Double,

    ) : ItemEntity

/**
 * A data class to hold a ProductEntity and its related parent entities (Category, Units, Store)
 * when queried from the database. This avoids the need for multiple separate queries.
 */
data class ProductWithDetails(
    @Embedded val product: ProductEntity,

    @Relation(parentColumn = "categoryId", entityColumn = "localId")
    val category: CategoryEntity?,

    @Relation(parentColumn = "mainUnitId", entityColumn = "localId")
    val mainUnit: UnitEntity?,

    @Relation(parentColumn = "subUnitId", entityColumn = "localId")
    val subUnit: UnitEntity?,
)

/**
 * Maps the rich ProductWithDetails object from the database to the clean Product domain model.
 */
fun ProductWithDetails.toDomain(): Product {
    return Product(
        id = Id(local = product.localId, server = product.serverId),
        name = LocalizedString(arName = product.arName, enName = product.enName),
        category = category?.toDomain(),
        averagePrice = product.averagePurchasePrice,
        sellingPrice = product.sellingPrice,
        subProductUnit = subUnit?.toDomain(),
        mainProductUnit = mainUnit!!.toDomain(),
        subUnitsPerMainUnit = product.subUnitsPerMainUnit,
        isSynced = product.isSynced,
        createdAt = product.createdAt,
        updatedAt = product.updatedAt,
        purchasePrice = product.purchasePrice,
        barcode = product.barcode ?: ""
    )
}
