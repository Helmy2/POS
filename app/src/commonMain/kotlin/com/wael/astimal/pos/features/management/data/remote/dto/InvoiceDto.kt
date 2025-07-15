package com.wael.astimal.pos.features.management.data.remote.dto


import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceType
import com.wael.astimal.pos.features.management.data.local.entity.PaymentMethod
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class InvoiceDto(
    @SerialName("id")
    val id: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("invoice_date")
    val invoiceDate: String,
    @SerialName("invoice_type")
    val invoiceType: String,
    @SerialName("total_amount")
    val totalAmount: Double,
    @SerialName("paid_amount")
    val paidAmount: Double,
    @SerialName("payment_method")
    val paymentMethod: String,
    @SerialName("store_id")
    val storeId: Long,
    @SerialName("employee_id")
    val employeeId: String,
    @SerialName("business_partner_id")
    val partnerId: String,
)

fun InvoiceDto.toEntity(
    partnerId: Long,
    employeeId: Long,
    storeId: Long
): InvoiceEntity {
    return InvoiceEntity(
        supabaseId = id,
        isSynced = true,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = updatedAt.parseIsoTimestamp() ?: Clock.now(),
        orderDate = invoiceDate.parseIsoTimestamp() ?: Clock.now(),
        businessPartnerId = partnerId,
        employeeId = employeeId,
        paidAmount = paidAmount,
        totalAmount = totalAmount,
        paymentMethod = PaymentMethod.valueOf(paymentMethod.uppercase()),
        invoiceType = InvoiceType.valueOf(invoiceType.uppercase()),
        storeId = storeId,
    )
}