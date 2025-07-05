package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetails
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrder
import com.wael.astimal.pos.features.management.domain.entity.PurchaseOrderItem
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain

@Entity(
    tableName = "purchases",
    foreignKeys = [ForeignKey(
        entity = BusinessPartnerEntity::class,
        parentColumns = ["localId"],
        childColumns = ["businessPartnerLocalId"],
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
    )],
    indices = [Index(value = ["businessPartnerLocalId"]), Index(value = ["employeeLocalId"]), Index(
        value = ["invoiceNumber"],
        unique = true
    )]
)
data class PurchaseEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,

    var invoiceNumber: String,

    val businessPartnerLocalId: Long,
    val employeeLocalId: Long,

    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
) : ItemEntity

@Entity(
    tableName = "purchase_products", foreignKeys = [ForeignKey(
        entity = PurchaseEntity::class,
        parentColumns = ["localId"],
        childColumns = ["purchaseLocalId"],
    ), ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["localId"],
        childColumns = ["productLocalId"],
    )], indices = [Index(value = ["purchaseLocalId"]), Index(value = ["productLocalId"])]
)
data class PurchaseProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Long?,
    val purchaseLocalId: Long,
    val productLocalId: Long,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)


data class PurchaseWithDetailsEntity(
    @Embedded val purchase: PurchaseEntity,

    @Relation(
        parentColumn = "businessPartnerLocalId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    ) val supplier: BusinessPartnerWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId",
        entityColumn = "id",
        entity = UserEntity::class
    ) val user: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "purchaseLocalId",
        entity = PurchaseProductEntity::class
    ) val itemsWithProductDetails: List<PurchaseProductItemWithDetails>
)

data class PurchaseProductItemWithDetails(
    @Embedded val purchaseItem: PurchaseProductEntity,

    @Relation(
        parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class
    ) val product: ProductWithDetails?,
)

fun PurchaseWithDetailsEntity.toDomain(): PurchaseOrder {
    return PurchaseOrder(
        id = Id(purchase.localId, purchase.serverId),
        invoiceNumber = purchase.invoiceNumber,
        supplier = supplier?.toDomain() ?: throw NullPointerException(),
        user = user?.toDomain() ?: throw NullPointerException(),
        amountRemaining = purchase.amountRemaining,
        totalAmount = purchase.totalAmount,
        amountPaid = purchase.amountPaid,
        paymentType = purchase.paymentType,
        data = purchase.createdAt,
        items = itemsWithProductDetails.map { it.toDomain() },
        isSynced = purchase.isSynced,
        createdAt = purchase.createdAt,
        updatedAt = purchase.updatedAt
    )
}

fun PurchaseProductItemWithDetails.toDomain(): PurchaseOrderItem {
    return PurchaseOrderItem(
        id = Id(purchaseItem.localId, purchaseItem.serverId),
        product = product?.toDomain() ?: throw NullPointerException(),
        quantity = purchaseItem.quantity,
        purchasePrice = purchaseItem.purchasePrice,
        itemTotalPrice = purchaseItem.itemTotalPrice
    )
}