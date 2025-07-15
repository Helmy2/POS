package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class ItemDto(
    @SerialName("id")
    val id: String,
    @SerialName("invoice_id")
    val invoiceId: String,
    @SerialName("product_id")
    val productId: Long,
    @SerialName("quantity")
    val quantity: Double,
    @SerialName("unit_price")
    val unitPrice: Double
)

fun ItemDto.toEntity(
    productId: Long
) = InvoiceItemEntity(
    supabaseId = id,
    isSynced = true,
    invoiceId = invoiceId,
    productId = productId,
    quantity = quantity,
    unitPrice = unitPrice
)