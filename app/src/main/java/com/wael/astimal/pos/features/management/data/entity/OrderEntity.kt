package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.entity.SalesOrderItem
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain

@Entity(
    tableName = "orders",
    foreignKeys = [ForeignKey(
        entity = ClientEntity::class,
        parentColumns = ["id"],
        childColumns = ["clientLocalId"],
        onDelete = ForeignKey.RESTRICT
    ), ForeignKey(
        entity = UserEntity::class,
        parentColumns = ["id"],
        childColumns = ["employeeLocalId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index(value = ["clientLocalId"]), Index(value = ["employeeLocalId"]), Index(
        value = ["invoiceNumber"], unique = true
    )]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    var invoiceNumber: String?,

    val clientLocalId: Long,
    val employeeLocalId: Long,

    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,

    val paymentType: PaymentType,
    val orderDate: Long,

    var isSynced: Boolean = false,
    var lastModified: Long = System.currentTimeMillis(),
    var isDeletedLocally: Boolean = false
)

@Entity(
    tableName = "order_products",
    foreignKeys = [ForeignKey(
        entity = OrderEntity::class,
        parentColumns = ["localId"],
        childColumns = ["orderLocalId"],
        onDelete = ForeignKey.CASCADE
    ), ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["localId"],
        childColumns = ["productLocalId"],
        onDelete = ForeignKey.RESTRICT
    ), ForeignKey(
        entity = UnitEntity::class,
        parentColumns = ["localId"],
        childColumns = ["unitLocalId"],
        onDelete = ForeignKey.RESTRICT
    )],
    indices = [Index(value = ["orderLocalId"]), Index(value = ["productLocalId"]), Index(value = ["unitLocalId"])]
)
data class OrderProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Int?,
    val orderLocalId: Long,
    val productLocalId: Long,
    val unitLocalId: Long,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
)


data class OrderWithDetailsEntity(
    @Embedded val order: OrderEntity,

    @Relation(
        parentColumn = "clientLocalId", entityColumn = "id", entity = ClientEntity::class
    ) val clientWithUser: ClientWithDetailsEntity?,

    @Relation(
        parentColumn = "employeeLocalId", entityColumn = "id", entity = UserEntity::class
    ) val employeeUser: UserEntity?,

    @Relation(
        parentColumn = "localId", entityColumn = "orderLocalId", entity = OrderProductEntity::class
    ) val itemsWithProductDetails: List<OrderProductItemWithDetails>
)

data class OrderProductItemWithDetails(
    @Embedded val orderItem: OrderProductEntity,

    @Relation(
        parentColumn = "productLocalId", entityColumn = "localId", entity = ProductEntity::class
    ) val product: ProductWithDetailsEntity?,

    @Relation(
        parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class
    ) val unit: UnitEntity?
)

fun OrderWithDetailsEntity.toDomain(): SalesOrder {
    return SalesOrder(
        localId = this.order.localId,
        serverId = this.order.serverId,
        invoiceNumber = this.order.invoiceNumber,
        amountPaid = this.order.amountPaid,
        amountRemaining = this.order.amountRemaining,
        totalAmount = this.order.totalAmount,
        paymentType = this.order.paymentType,
        data = this.order.orderDate,
        items = this.itemsWithProductDetails.map { it.toDomain() },
        isSynced = this.order.isSynced,
        lastModified = this.order.lastModified,
        isDeletedLocally = this.order.isDeletedLocally,
        client = this.clientWithUser?.toDomain(),
        employee = this.employeeUser?.toDomain(),
    )
}

fun OrderProductItemWithDetails.toDomain(): SalesOrderItem {
    return SalesOrderItem(
        localId = this.orderItem.localId,
        serverId = this.orderItem.serverId,
        orderLocalId = this.orderItem.orderLocalId,
        product = this.product?.toDomain(),
        productUnit = this.unit?.toDomain(),
        quantity = this.orderItem.quantity,
        unitSellingPrice = this.orderItem.unitSellingPrice,
        itemTotalPrice = this.orderItem.itemTotalPrice,
    )
}

data class DailySaleData(
    val saleDate: String,
    val totalRevenue: Double,
    val numberOfSales: Int
)