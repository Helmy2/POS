package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturn
import com.wael.astimal.pos.features.management.domain.entity.PurchaseReturnItem
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "purchase_returns",
    foreignKeys = [
        ForeignKey(
            entity = SupplierEntity::class,
            parentColumns = ["id"],
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
data class PurchaseReturnEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    var invoiceNumber: String?,
    val supplierLocalId: Long?,
    val employeeLocalId: Long?, // from 'employee_id'
    val totalPrice: Double,
    val paymentType: PaymentType,
    val returnDate: Long,      // from 'date'
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "purchase_return_products",
    foreignKeys = [
        ForeignKey(
            entity = PurchaseReturnEntity::class,
            parentColumns = ["localId"],
            childColumns = ["purchaseReturnLocalId"],
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
    indices = [Index(value = ["purchaseReturnLocalId"]), Index(value = ["productLocalId"]), Index(value = ["unitLocalId"])]
)
data class PurchaseReturnProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val purchaseReturnLocalId: Long,
    val productLocalId: Long,
    val unitLocalId: Long,
    val quantity: Double,
    val purchasePrice: Double,
    val itemTotalPrice: Double
)

// --- Data Layer: POJOs for Relational Queries ---
data class PurchaseReturnWithDetailsEntity(
    @Embedded
    val purchaseReturn: PurchaseReturnEntity,
    @Relation(parentColumn = "supplierLocalId", entityColumn = "id", entity = SupplierEntity::class)
    val supplier: SupplierWithDetailsEntity?,
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
    @Relation(parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class)
    val unit: UnitEntity?
)

fun PurchaseReturnWithDetailsEntity.toDomain(): PurchaseReturn {
    return PurchaseReturn(
        localId = this.purchaseReturn.localId,
        serverId = this.purchaseReturn.serverId,
        invoiceNumber = this.purchaseReturn.invoiceNumber,
        supplier = this.supplier?.toDomain(),
        employee = this.employee?.toDomain(),
        totalPrice = this.purchaseReturn.totalPrice,
        paymentType = this.purchaseReturn.paymentType,
        returnDate = this.purchaseReturn.returnDate,
        items = this.itemsWithProductDetails.map { it.toDomain() },
        isSynced = this.purchaseReturn.isSynced,
        lastModified = this.purchaseReturn.lastModified,
        isDeletedLocally = this.purchaseReturn.isDeletedLocally
    )
}

fun PurchaseReturnItemWithDetails.toDomain(): PurchaseReturnItem {
    return PurchaseReturnItem(
        localId = this.purchaseReturnItem.localId,
        serverId = this.purchaseReturnItem.serverId,
        purchaseReturnLocalId = this.purchaseReturnItem.purchaseReturnLocalId,
        product = this.product?.toDomain(),
        unit = this.unit?.toDomain(),
        quantity = this.purchaseReturnItem.quantity,
        purchasePrice = this.purchaseReturnItem.purchasePrice,
        itemTotalPrice = this.purchaseReturnItem.itemTotalPrice
    )
}
