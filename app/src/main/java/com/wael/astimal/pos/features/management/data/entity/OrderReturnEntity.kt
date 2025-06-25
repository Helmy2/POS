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
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.entity.SalesReturnItem
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "order_returns",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["localId"],
            childColumns = ["clientLocalId"],
        ),
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeLocalId"],
        )
    ],
    indices = [
        Index(value = ["clientLocalId"]),
        Index(value = ["employeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class OrderReturnEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,
    val invoiceNumber: String,
    val clientLocalId: Long,
    val employeeLocalId: Long,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
    val paymentType: PaymentType,
) : ItemEntity

@Entity(
    tableName = "order_return_products",
    foreignKeys = [
        ForeignKey(
            entity = OrderReturnEntity::class,
            parentColumns = ["localId"],
            childColumns = ["orderReturnLocalId"],
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productLocalId"],
        )
    ],
    indices = [Index(value = ["orderReturnLocalId"]), Index(value = ["productLocalId"])]
)
data class OrderReturnProductEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Long?,
    val orderReturnLocalId: Long,
    val productLocalId: Long,
    val quantity: Double,
    val priceAtReturn: Double,
    val itemTotalValue: Double,
)

data class OrderReturnWithDetailsEntity(
    @Embedded
    val orderReturn: OrderReturnEntity,

    @Relation(parentColumn = "clientLocalId", entityColumn = "localId", entity = ClientEntity::class)
    val clientWithUser: ClientWithDetailsEntity?,

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
    val product: ProductWithDetailsEntity?,
)


fun OrderReturnWithDetailsEntity.toDomain(): SalesReturn {
    return SalesReturn(
        id = Id(orderReturn.localId, orderReturn.serverId),
        invoiceNumber = orderReturn.invoiceNumber,
        client = clientWithUser?.toDomain() ?: throw NullPointerException(),
        employee = employeeUser?.toDomain() ?: throw NullPointerException(),
        amountPaid = orderReturn.amountPaid,
        amountRemaining = orderReturn.amountRemaining,
        totalAmount = orderReturn.totalAmount,
        paymentType = orderReturn.paymentType,
        items = itemsWithProductDetails.map { it.toDomain() },
        isSynced = orderReturn.isSynced,
        createdAt = orderReturn.createdAt,
        updatedAt = orderReturn.updatedAt
    )
}

fun OrderReturnItemWithDetails.toDomain(): SalesReturnItem {
    return SalesReturnItem(
        id = Id(returnItem.localId, returnItem.serverId),
        product = product?.toDomain() ?: throw NullPointerException(),
        quantity = returnItem.quantity,
        priceAtReturn = returnItem.priceAtReturn,
        itemTotalValue = returnItem.itemTotalValue,
    )
}