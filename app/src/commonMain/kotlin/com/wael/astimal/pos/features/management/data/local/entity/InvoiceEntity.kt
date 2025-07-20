package com.wael.astimal.pos.features.management.data.local.entity

import androidx.room.Embedded
import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey
import androidx.room.Relation
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.ProductWithDetails
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreEntity
import com.wael.astimal.pos.features.inventory.data.local.entity.StoreWithDetails
import com.wael.astimal.pos.features.inventory.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.data.remote.dto.ItemDto
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import com.wael.astimal.pos.features.management.domain.entity.InvoiceItem
import com.wael.astimal.pos.features.user.data.local.entity.UserEntity
import com.wael.astimal.pos.features.user.data.local.entity.toDomain
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.payment_type_card
import pos.app.generated.resources.payment_type_cash
import pos.app.generated.resources.payment_type_other
import pos.app.generated.resources.payment_type_transfer

@Entity(
    tableName = "invoices",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["id"],
            childColumns = ["employeeId"],
        ),
        ForeignKey(
            entity = StoreEntity::class,
            parentColumns = ["localId"],
            childColumns = ["storeId"],
        ),
        ForeignKey(
            entity = BusinessPartnerEntity::class,
            parentColumns = ["localId"],
            childColumns = ["businessPartnerId"],
        )
    ],
    indices = [
        Index("supabaseId", unique = true),
        Index("employeeId"),
        Index("storeId"),
        Index("businessPartnerId")
    ]
)
data class InvoiceEntity(
    @PrimaryKey
    val supabaseId: String,
    var isSynced: Boolean = false,
    val createdAt: Long,
    var updatedAt: Long,
    var orderDate: Long,
    var isDeletedLocally: Boolean = false,
    val invoiceType: InvoiceType,
    val employeeId: String,
    val storeId: String,
    val businessPartnerId: String?,
    val totalAmount: Double,
    val paidAmount: Double,
    val paymentMethod: PaymentMethod
)

@Entity(
    tableName = "invoice_items",
    foreignKeys = [
        ForeignKey(
            entity = InvoiceEntity::class,
            parentColumns = ["supabaseId"],
            childColumns = ["invoiceId"],
        ),
        ForeignKey(
            entity = ProductEntity::class,
            parentColumns = ["localId"],
            childColumns = ["productId"],
        )
    ],
    indices = [
        Index("supabaseId", unique = true),
        Index("invoiceId"),
        Index("productId")
    ]
)
data class InvoiceItemEntity(
    @PrimaryKey
    val supabaseId: String,
    var isSynced: Boolean = false,
    var isDeletedLocally: Boolean = false,

    val invoiceId: String,
    val productId: String,
    val quantity: Double,
    val unitPrice: Double
)

data class InvoiceItemEntityWithDetails(
    @Embedded val item: InvoiceItemEntity,

    @Relation(
        parentColumn = "productId", entityColumn = "localId", entity = ProductEntity::class
    ) val product: ProductWithDetails,
)

data class InvoiceWithItems(
    @Embedded val invoice: InvoiceEntity,
    @Relation(
        parentColumn = "supabaseId",
        entityColumn = "invoiceId",
        entity = InvoiceItemEntity::class
    )
    val items: List<InvoiceItemEntityWithDetails>,

    @Relation(
        parentColumn = "employeeId",
        entityColumn = "id"
    )
    val employee: UserEntity,

    @Relation(
        parentColumn = "storeId",
        entityColumn = "localId",
        entity = StoreEntity::class
    )
    val store: StoreWithDetails,

    @Relation(
        parentColumn = "businessPartnerId",
        entityColumn = "localId",
        entity = BusinessPartnerEntity::class
    )
    val partner: BusinessPartnerWithDetailsEntity?
)

fun InvoiceWithItems.toDomain(): Invoice {
    return Invoice(
        id = invoice.supabaseId,
        isSynced = invoice.isSynced,
        createdAt = invoice.createdAt,
        updatedAt = invoice.updatedAt,
        paidAmount = invoice.paidAmount,
        totalAmount = invoice.totalAmount,
        paymentMethod = invoice.paymentMethod,
        invoiceType = invoice.invoiceType,
        partner = partner!!.toDomain(),
        employee = employee.toDomain(),
        store = store.toDomain(),
        orderDate = invoice.orderDate,
        items = items.map {
            InvoiceItem(
                id = it.item.supabaseId,
                isSynced = it.item.isSynced,
                product = it.product.toDomain(),
                quantity = it.item.quantity,
                unitPrice = it.item.unitPrice,
            )
        }
    )
}

fun InvoiceItemEntity.toDto(): ItemDto {
    return ItemDto(
        id = supabaseId,
        productId = productId,
        quantity = quantity,
        unitPrice = unitPrice,
        invoiceId = invoiceId
    )
}


enum class InvoiceType {
    SALES,
    PURCHASE,
    SALES_RETURN,
    PURCHASE_RETURN
}

enum class PaymentMethod {
    CASH,
    CARD,
    BANK_TRANSFER,
    OTHER;

    fun stringResource(): StringResource {
        return when (this) {
            CASH -> Res.string.payment_type_cash
            CARD -> Res.string.payment_type_card
            BANK_TRANSFER -> Res.string.payment_type_transfer
            OTHER -> Res.string.payment_type_other
        }
    }
}


