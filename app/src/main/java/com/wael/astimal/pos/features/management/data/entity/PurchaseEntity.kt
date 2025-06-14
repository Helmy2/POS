package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrderItem
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "purchases",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["localId"],
            childColumns = ["supplierLocalId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeLocalId"],
            onDelete = ForeignKey.RESTRICT
        )
    ],
    indices = [
        Index(value = ["supplierLocalId"]),
        Index(value = ["employeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    var invoiceNumber: String?,

    val supplierLocalId: Long?,
    val employeeLocalId: Long?,

    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
    val purchaseDate: Long,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "purchase_products",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseEntity::class,
            parentColumns = ["localId"],
            childColumns = ["purchaseLocalId"],
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
    ],
    indices = [Index(value = ["purchaseLocalId"]), Index(value = ["productLocalId"]), Index(value = ["unitLocalId"])]
)
data class PurchaseProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val purchaseLocalId: Long,
    val productLocalId: Long,
    val unitLocalId: Long,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)


data class PurchaseWithDetailsEntity(
    @Embedded
    val purchase: PurchaseEntity,

    @Relation(parentColumn = "supplierLocalId", entityColumn = "localId", entity = SupplierEntity::class)
    val supplier: SupplierWithDetailsEntity?,

    @Relation(parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class)
    val user: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "purchaseLocalId",
        entity = PurchaseProductEntity::class
    )
    val itemsWithProductDetails: List<PurchaseProductItemWithDetails>
)

data class PurchaseProductItemWithDetails(
    @Embedded
    val purchaseItem: PurchaseProductEntity,

    @Relation(
        parentColumn = "productLocalId",
        entityColumn = "localId",
        entity = ProductEntity::class
    )
    val product: ProductWithDetailsEntity?,

    @Relation(parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class)
    val unit: UnitEntity?
)

fun PurchaseWithDetailsEntity.toDomain(): PurchaseOrder {
    return PurchaseOrder(
        localId = this.purchase.localId,
        serverId = this.purchase.serverId,
        invoiceNumber = this.purchase.invoiceNumber,
        supplier = this.supplier?.toDomain(),
        user = this.user?.toDomain(),
        amountRemaining = this.purchase.amountRemaining,
        totalAmount = this.purchase.totalAmount,
        amountPaid = this.purchase.amountPaid,
        paymentType = this.purchase.paymentType,
        data = this.purchase.purchaseDate,
        items = this.itemsWithProductDetails.map { it.toDomain() },
        isSynced = this.purchase.isSynced,
        lastModified = this.purchase.lastModified,
        isDeletedLocally = this.purchase.isDeletedLocally
    )
}

fun PurchaseProductItemWithDetails.toDomain(): PurchaseOrderItem {
    return PurchaseOrderItem(
        localId = this.purchaseItem.localId,
        serverId = this.purchaseItem.serverId,
        purchaseLocalId = this.purchaseItem.purchaseLocalId,
        product = this.product?.toDomain(),
        productUnit = this.unit?.toDomain(),
        quantity = this.purchaseItem.quantity,
        purchasePrice = this.purchaseItem.purchasePrice,
        itemTotalPrice = this.purchaseItem.itemTotalPrice
    )
}