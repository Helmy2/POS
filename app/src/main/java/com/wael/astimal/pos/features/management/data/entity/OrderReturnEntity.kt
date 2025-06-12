package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.management.domain.entity.PaymentType
import com.wael.astimal.pos.features.management.domain.entity.SalesReturn
import com.wael.astimal.pos.features.management.domain.entity.SalesReturnItem
import com.wael.astimal.pos.features.inventory.data.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.entity.ProductWithDetailsEntity
import com.wael.astimal.pos.features.inventory.data.entity.UnitEntity
import com.wael.astimal.pos.features.inventory.data.entity.toDomain
import com.wael.astimal.pos.features.user.data.entity.UserEntity
import com.wael.astimal.pos.features.user.data.entity.toDomain


@Entity(
    tableName = "order_returns",
    foreignKeys = [
        ForeignKey(
            entity = ClientEntity::class,
            parentColumns = ["id"],
            childColumns = ["clientLocalId"],
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
        Index(value = ["employeeLocalId"]),
        Index(value = ["invoiceNumber"], unique = true)
    ]
)
data class OrderReturnEntity(
    @PrimaryKey(autoGenerate = true)
    val localId: Long = 0L,
    val serverId: Int?,
    val invoiceNumber: String?,
    val clientLocalId: Long,
    val employeeLocalId: Long?,
    val previousDebt: Double?,
    val amountPaid: Double,
    val amountRemaining: Double,
    val totalAmount: Double,
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
)

data class OrderReturnWithDetailsEntity(
    @Embedded
    val orderReturn: OrderReturnEntity,

    @Relation(parentColumn = "clientLocalId", entityColumn = "id", entity = ClientEntity::class)
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

    @Relation(parentColumn = "unitLocalId", entityColumn = "localId", entity = UnitEntity::class)
    val unit: UnitEntity?
)


fun OrderReturnWithDetailsEntity.toDomain(): SalesReturn {
    return SalesReturn(
        localId = this.orderReturn.localId,
        serverId = this.orderReturn.serverId,
        invoiceNumber = this.orderReturn.invoiceNumber,
        client = this.clientWithUser?.toDomain(),
        employee = this.employeeUser?.toDomain(),
        previousDebt = this.orderReturn.previousDebt,
        amountPaid = this.orderReturn.amountPaid,
        amountRemaining = this.orderReturn.amountRemaining,
        totalReturnedValue = this.orderReturn.totalAmount,
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
        product = this.product?.toDomain(),
        productUnit= this.unit?.toDomain(),
        quantity = this.returnItem.quantity,
        priceAtReturn = this.returnItem.priceAtReturn,
        itemTotalValue = this.returnItem.itemTotalValue,
    )
}