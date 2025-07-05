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
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturnItem
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain

@Entity(
    tableName = "purchase_returns",
    foreignKeys = [
        ForeignKey(
            entity = BusinessPartnerEntity::class,
            parentColumns = ["localId"],
            childColumns = ["businessPartnerLocalId"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeLocalId"],
        )
    ],
    indices = [
        Index(value = ["businessPartnerLocalId"]),
        Index(value = ["employeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class PurchaseReturnEntity(
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
    tableName = "purchase_return_products",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseReturnEntity::class,
            parentColumns = ["localId"],
            childColumns = ["purchaseReturnLocalId"],
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productLocalId"],
        )
    ],
    indices = [Index(value = ["purchaseReturnLocalId"]), Index(value = ["productLocalId"])]
)
data class PurchaseReturnProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Long?,
    val purchaseReturnLocalId: Long,
    val productLocalId: Long,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)

// --- Data Layer: POJOs for Relational Queries ---
data class PurchaseReturnWithDetailsEntity(
    @Embedded
    val purchaseReturn: PurchaseReturnEntity,
    @Relation(
        parentColumn = "businessPartnerLocalId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    )
    val supplier: BusinessPartnerWithDetailsEntity?,
    @Relation(parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class)
    val employee: UserEntity?,
    @Relation(
        parentColumn = "localId",
        entityColumn = "purchaseReturnLocalId",
        entity = PurchaseReturnProductEntity::class
    )
    val itemsWithProductDetails: List<PurchaseReturnItemWithDetails>
)

data class PurchaseReturnItemWithDetails(
    @Embedded
    val purchaseReturnItem: PurchaseReturnProductEntity,
    @Relation(parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class)
    val product: ProductWithDetailsEntity?,
)

fun PurchaseReturnWithDetailsEntity.toDomain(): PurchaseReturn {
    return PurchaseReturn(
        id = Id(purchaseReturn.localId, purchaseReturn.serverId),
        invoiceNumber = purchaseReturn.invoiceNumber,
        supplier = supplier?.toDomain() ?: throw NullPointerException(),
        employee = employee?.toDomain() ?: throw NullPointerException(),
        amountRemaining = purchaseReturn.amountRemaining,
        amountPaid = purchaseReturn.amountPaid,
        totalAmount = purchaseReturn.totalAmount,
        paymentType = purchaseReturn.paymentType,
        data = purchaseReturn.createdAt,
        items = itemsWithProductDetails.map { it.toDomain() },
        isSynced = purchaseReturn.isSynced,
        createdAt = purchaseReturn.createdAt,
        updatedAt = purchaseReturn.updatedAt
    )
}

fun PurchaseReturnItemWithDetails.toDomain(): PurchaseReturnItem {
    return PurchaseReturnItem(
        id = Id(purchaseReturnItem.localId, purchaseReturnItem.serverId),
        product = product?.toDomain() ?: throw NullPointerException(),
        quantity = purchaseReturnItem.quantity,
        purchasePrice = purchaseReturnItem.purchasePrice,
        itemTotalPrice = purchaseReturnItem.itemTotalPrice
    )
}
