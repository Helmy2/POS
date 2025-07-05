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
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductWithDetails
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesOrder
import com.wael.astimal.pos.features.management.domain.entity.SalesOrderItem
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain

@Entity(
    tableName = "orders",
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
        value = ["invoiceNumber"], unique = true
    )]
)
data class OrderEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,
    val orderDate: Long,
    val invoiceNumber: String,
    val businessPartnerLocalId: Long,
    val employeeLocalId: Long,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
) : ItemEntity

@Entity(
    tableName = "order_products",
    foreignKeys = [ForeignKey(
        entity = OrderEntity::class,
        parentColumns = ["localId"],
        childColumns = ["orderLocalId"],
    ), ForeignKey(
        entity = ProductEntity::class,
        parentColumns = ["localId"],
        childColumns = ["productLocalId"],
    )],
    indices = [Index(value = ["orderLocalId"]), Index(value = ["productLocalId"])]
)
data class OrderProductEntity(
    @PrimaryKey(autoGenerate = true) val localId: Long = 0L,
    val serverId: Long?,
    val orderLocalId: Long,
    val productLocalId: Long,
    val quantity: Double,
    val unitSellingPrice: Double,
    val itemTotalPrice: Double,
)


data class OrderWithDetailsEntity(
    @Embedded val order: OrderEntity,

    @Relation(
        parentColumn = "businessPartnerLocalId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    ) val clientWithUser: BusinessPartnerWithDetailsEntity?,

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
    ) val product: ProductWithDetails?,
)

fun OrderWithDetailsEntity.toDomain(): SalesOrder {
    return SalesOrder(
        id = Id(order.localId, order.serverId),
        invoiceNumber = order.invoiceNumber,
        amountPaid = order.amountPaid,
        amountRemaining = order.amountRemaining,
        totalAmount = order.totalAmount,
        paymentType = order.paymentType,
        items = itemsWithProductDetails.map { it.toDomain() },
        isSynced = order.isSynced,
        client = clientWithUser?.toDomain() ?: throw NullPointerException(),
        employee = employeeUser?.toDomain() ?: throw NullPointerException(),
        createdAt = order.createdAt,
        updatedAt = order.updatedAt,
        orderDate = order.orderDate
    )
}

fun OrderProductItemWithDetails.toDomain(): SalesOrderItem {
    return SalesOrderItem(
        id = Id(orderItem.localId, orderItem.serverId),
        product = product?.toDomain() ?: throw NullPointerException(),
        quantity = orderItem.quantity,
        unitSellingPrice = orderItem.unitSellingPrice,
        itemTotalPrice = orderItem.itemTotalPrice,
    )
}

data class DailySaleData(
    val saleDate: String,
    val totalRevenue: Double,
    val numberOfSales: Int
)