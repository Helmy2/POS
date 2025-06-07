package com.wael.astimal.pos.features.client_management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.client_management.domain.entity.PaymentType
import com.wael.astimal.pos.features.client_management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.client_management.domain.entity.SalesOrderItem
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.domain.entity.LocalizedString
import com.wael.astimal.pos.features.user.data.entity.UserEntity

@Entity(
    tableName = "orders",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["localId"],
            childColumns = ["clientLocalId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeLocalId"],
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["mainEmployeeLocalId"],
            onDelete = ForeignKey.SET_NULL
        )
    ],
    indices = [
        Index(value = ["clientLocalId"]),
        Index(value = ["employeeLocalId"]),
        Index(value = ["mainEmployeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    var invoiceNumber: String?,

    val clientLocalId: Long,
    val employeeLocalId: Long,
    val mainEmployeeLocalId: Long?,

    val previousClientDebt: Double?,
    val amountPaid: Double = 0.0,
    val amountRemaining: Double = 0.0,
    val totalPrice: Double = 0.0,
    val totalGain: Double = 0.0,

    val paymentType: PaymentType,
    val orderDate: Long,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "order_products",
    foreignKeys = [
        ForeignKey(
            entity = OrderEntity::class,
            parentColumns = ["localId"],
            childColumns = ["orderLocalId"],
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
    indices = [Index(value = ["orderLocalId"]), Index(value = ["productLocalId"]), Index(value = ["unitLocalId"])]
)
data class OrderProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val orderLocalId: Long,
    val productLocalId: Long,
    val unitLocalId: Long,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
    val itemGain: Double
)


data class OrderWithDetailsEntity(
    @Embedded
    val order: OrderEntity,

    @Relation(parentColumn = "clientLocalId", entityColumn = "localId", entity = ClientEntity::class)
    val clientWithUser: ClientWithDetailsEntity?,

    @Relation(parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class)
    val employeeUser: UserEntity?,

    @Relation(parentColumn = "mainEmployeeLocalId", entityColumn = "id", entity = UserEntity::class)
    val mainEmployeeUser: UserEntity?,

    @Relation(
        parentColumn = "localId",
        entityColumn = "orderLocalId",
        entity = OrderProductEntity::class
    )
    val itemsWithProductDetails: List<OrderProductItemWithDetails>
)

data class OrderProductItemWithDetails(
    @Embedded
    val orderItem: OrderProductEntity,

    @Relation(parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class)
    val product: ProductEntity?,

    @Relation(parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class)
    val unit: UnitEntity?
)

fun OrderWithDetailsEntity.toDomain(): SalesOrder {
    val clientName = this.clientWithUser?.clientUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    } ?: LocalizedString("Deleted", "Deleted")

    val employeeName = this.employeeUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    } ?: LocalizedString("Unknown", "Unknown")

    val mainEmployeeName = this.mainEmployeeUser?.let {
        LocalizedString(arName = it.arName, enName = it.enName ?: it.name)
    }

    return SalesOrder(
        localId = this.order.localId,
        serverId = this.order.serverId,
        invoiceNumber = this.order.invoiceNumber,
        clientLocalId = this.order.clientLocalId,
        clientName = clientName,
        employeeLocalId = this.order.employeeLocalId,
        employeeName = employeeName,
        mainEmployeeLocalId = this.order.mainEmployeeLocalId,
        mainEmployeeName = mainEmployeeName,
        previousClientDebt = this.order.previousClientDebt,
        amountPaid = this.order.amountPaid,
        amountRemaining = this.order.amountRemaining,
        totalPrice = this.order.totalPrice,
        totalGain = this.order.totalGain,
        paymentType = this.order.paymentType,
        orderDate = this.order.orderDate,
        items = this.itemsWithProductDetails.map { it.toDomain() },
        isSynced = this.order.isSynced,
        lastModified = this.order.lastModified,
        isDeletedLocally = this.order.isDeletedLocally
    )
}

fun OrderProductItemWithDetails.toDomain(): SalesOrderItem {
    return SalesOrderItem(
        localId = this.orderItem.localId,
        serverId = this.orderItem.serverId,
        orderLocalId = this.orderItem.orderLocalId,
        productLocalId = this.orderItem.productLocalId,
        productName = LocalizedString(arName = this.product?.arName, enName = this.product?.enName),
        unitLocalId = this.orderItem.unitLocalId,
        unitName = LocalizedString(arName = this.unit?.arName, enName = this.unit?.enName),
        quantity = this.orderItem.quantity,
        unitSellingPrice = this.orderItem.unitSellingPrice,
        itemTotalPrice = this.orderItem.itemTotalPrice,
        itemGain = this.orderItem.itemGain
    )
}