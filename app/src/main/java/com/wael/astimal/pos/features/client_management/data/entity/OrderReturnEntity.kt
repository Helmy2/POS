package com.wael.astimal.pos.features.client_management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.client_management.domain.entity.PaymentType
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.client_management.domain.entity.SalesReturnItem
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.data.entity.UserEntity


@Entity(
    tableName = "order_returns",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["localId"],
            childColumns = ["clientLocalId"],
            onDelete = ForeignKey.RESTRICT
        ),
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
        Index(value = ["clientLocalId"]),
        Index(value = ["supplierLocalId"]),
        Index(value = ["employeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class OrderReturnEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val invoiceNumber: String?,
    val clientLocalId: Long?,
    val supplierLocalId: Long?,
    val employeeLocalId: Long?,
    val previousDebt: Double?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalReturnedValue: Double,
    val totalGainLoss: Double,
    val paymentType: PaymentType,
    val returnDate: Long,
    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "order_return_products",
    foreignKeys = [
        ForeignKey(
            entity = OrderReturnEntity::class,
            parentColumns = ["localId"],
            childColumns = ["orderReturnLocalId"],
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
    indices = [Index(value = ["orderReturnLocalId"]), Index(value = ["productLocalId"]), Index(value = ["unitLocalId"])]
)
data class OrderReturnProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val orderReturnLocalId: Long,
    val productLocalId: Long,
    val unitLocalId: Long,
    val quantity: Double,
    val priceAtReturn: Double,
    val itemTotalValue: Double,
    val itemGainLoss: Double
)

data class OrderReturnWithDetailsEntity(
    @Embedded
    val orderReturn: OrderReturnEntity,

    @Relation(parentColumn = "clientLocalId", entityColumn = "localId", entity = ClientEntity::class)
    val clientWithUser: ClientWithDetailsEntity?,

    @Relation(parentColumn = "supplierLocalId", entityColumn = "localId", entity = SupplierEntity::class)
    val supplier: SupplierEntity?,

    @Relation(parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class)
    val employeeUser: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "orderReturnLocalId",
        entity = OrderReturnProductEntity::class
    )
    val itemsWithProductDetails: List<OrderReturnItemWithDetails>
)

data class OrderReturnItemWithDetails(
    @Embedded
    val returnItem: OrderReturnProductEntity,

    @Relation(parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class)
    val product: ProductEntity?,

    @Relation(parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class)
    val unit: UnitEntity?
)


fun OrderReturnWithDetailsEntity.toDomain(): SalesReturn {
    val clientName = this.clientWithUser?.clientUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    }

    val employeeName = this.employeeUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    } ?: LocalizedString("Unknown", "Unknown")

    return SalesReturn(
        localId = this.orderReturn.localId,
        serverId = this.orderReturn.serverId,
        invoiceNumber = this.orderReturn.invoiceNumber,
        clientLocalId = this.orderReturn.clientLocalId,
        clientName = clientName,
        supplierLocalId = this.orderReturn.supplierLocalId,
        supplierName = this.supplier?.name, // From the related SupplierEntity
        employeeLocalId = this.orderReturn.employeeLocalId,
        employeeName = employeeName,
        previousDebt = this.orderReturn.previousDebt,
        amountPaid = this.orderReturn.amountPaid,
        amountRemaining = this.orderReturn.amountRemaining,
        totalReturnedValue = this.orderReturn.totalReturnedValue,
        totalGainLoss = this.orderReturn.totalGainLoss,
        paymentType = this.orderReturn.paymentType,
        returnDate = this.orderReturn.returnDate,
        items = this.itemsWithProductDetails.map { it.toDomain() },
        isSynced = this.orderReturn.isSynced,
        lastModified = this.orderReturn.lastModified,
        isDeletedLocally = this.orderReturn.isDeletedLocally
    )
}

fun OrderReturnItemWithDetails.toDomain(): SalesReturnItem {
    return SalesReturnItem(
        localId = this.returnItem.localId,
        serverId = this.returnItem.serverId,
        returnLocalId = this.returnItem.orderReturnLocalId,
        productLocalId = this.returnItem.productLocalId,
        productName = LocalizedString(arName = this.product?.arName, enName = this.product?.enName),
        unitLocalId = this.returnItem.unitLocalId,
        unitName = LocalizedString(arName = this.unit?.arName, enName = this.unit?.enName),
        quantity = this.returnItem.quantity,
        priceAtReturn = this.returnItem.priceAtReturn,
        itemTotalValue = this.returnItem.itemTotalValue,
        itemGainLoss = this.returnItem.itemGainLoss
    )
}