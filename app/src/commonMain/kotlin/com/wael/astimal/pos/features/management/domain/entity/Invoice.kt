package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toISOString
import com.wael.astimal.pos.features.inventory.domain.entity.Product
import com.wael.astimal.pos.features.inventory.domain.entity.Store
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import com.wael.astimal.pos.features.management.data.remote.dto.InvoiceDto
import com.wael.astimal.pos.features.management.data.remote.dto.ItemDto
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
    val id: String,
    val createdAt: Long,
    val updatedAt: Long = Clock.now(),
    val isSynced: Boolean = false,
)

data class InvoiceItem(
    val id: String,
    val product: Product,
    val quantity: Double,
    val isSynced: Boolean,
    val unitPrice: Double,
)

fun Invoice.toEntity(): Pair<InvoiceEntity, List<InvoiceItemEntity>> {
    return InvoiceEntity(
        supabaseId = id,
        isSynced = isSynced,
        createdAt = createdAt,
        updatedAt = updatedAt,
        businessPartnerId = partner.id,
        employeeId = employee.id,
        paidAmount = paidAmount,
        totalAmount = totalAmount,
        paymentMethod = paymentMethod,
        invoiceType = invoiceType,
        storeId = store.id,
        orderDate = orderDate,
    ) to items.map {
        it.toEntity(id)
    }
}

fun InvoiceItem.toEntity(
    invoiceId: String
): InvoiceItemEntity {
    return InvoiceItemEntity(
        supabaseId = id,
        invoiceId = invoiceId,
        productId = product.id,
        quantity = quantity,
        unitPrice = unitPrice,
        isSynced = isSynced
    )
}


fun Invoice.matchesQuery(query: String): Boolean {
    val lowerQuery = query.lowercase()
    return id.contains(lowerQuery) ||
            partner.name.contains(lowerQuery) ||
            employee.name.contains(lowerQuery) ||
            store.name.contains(lowerQuery) ||
            items.any { it.product.name.contains(lowerQuery) }

}

fun InvoiceItem.toDto(
    invoiceId: String,
    updatedAt: String
): ItemDto {
    return ItemDto(
        id = id,
        productId = product.id,
        quantity = quantity,
        unitPrice = unitPrice,
        invoiceId = invoiceId,
        updatedAt = updatedAt,
    )
}

fun Invoice.toDto(updatedAt: String): InvoiceDto {
    return InvoiceDto(
        id = id,
        createdAt = createdAt.toISOString(),
        updatedAt = updatedAt,
        partnerId = partner.id,
        employeeId = employee.id,
        paidAmount = paidAmount,
        totalAmount = totalAmount,
        paymentMethod = paymentMethod.name.lowercase(),
        invoiceType = invoiceType.name.lowercase(),
        storeId = store.id,
        invoiceDate = orderDate.toISOString(),
    )
}
