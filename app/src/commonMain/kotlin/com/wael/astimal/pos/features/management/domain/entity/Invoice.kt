package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import com.wael.astimal.pos.features.user.domain.entity.User


data class Invoice(
    val partner: BusinessPartner,
    val employee: User,
    val store: Store,
    val paidAmount: Double,
    val totalAmount: Double,
    val paymentMethod: PaymentMethod,
    val invoiceType: InvoiceType,
    val items: List<InvoiceItem>,
    val orderDate: Long,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

data class InvoiceItem(
    val id: Id,
    val product: Product,
    val quantity: Double,
    val isSynced: Boolean,
    val unitPrice: Double,
)

fun Invoice.toEntity(): Pair<InvoiceEntity, List<InvoiceItemEntity>> {
    return InvoiceEntity(
        localId = id.local,
        supabaseId = id.serverStringId,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        businessPartnerId = partner.id.local,
        employeeId = employee.id.local,
        paidAmount = paidAmount,
        totalAmount = totalAmount,
        paymentMethod = paymentMethod,
        invoiceType = invoiceType,
        storeId = store.id.local,
        orderDate = orderDate,
    ) to items.map {
        it.toEntity(id.local)
    }
}

fun InvoiceItem.toEntity(
    invoiceId: Long
): InvoiceItemEntity {
    return InvoiceItemEntity(
        localId = id.local,
        supabaseId = id.serverStringId,
        invoiceId = invoiceId,
        productId = product.id.local,
        quantity = quantity,
        unitPrice = unitPrice,
    )
}
