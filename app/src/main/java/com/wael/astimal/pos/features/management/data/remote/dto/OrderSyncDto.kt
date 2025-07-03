package com.wael.astimal.pos.features.management.data.remote.dto

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class OrderSyncData(
    val orders: List<OrderDto>,
    @SerialName("date")
    val nextSyncDate: String
)

@Serializable
data class OrderDto(
    val id: Long,
    @SerialName("invoice_number")
    val invoiceNumber: String,
    @SerialName("source_id")
    val sourceId: Long,
    @SerialName("source_type")
    val sourceType: String, // "client" or "supplier"
    @SerialName("employee_id")
    val employeeId: Long,
    val paid: Double,
    val remaining: Double,
    @SerialName("total_price")
    val totalPrice: Double,
    @SerialName("payment_type")
    val paymentType: String,
    val date: String,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("order_products")
    val orderProducts: List<OrderProductDto>
)

@Serializable
data class OrderProductDto(
    val id: Long,
    @SerialName("product_id")
    val productId: Long,
    @SerialName("order_id")
    val orderId: Long,
    val quantity: Double,
    @SerialName("unit_id")
    val unitId: Long,
    val price: Double,
    @SerialName("total_price")
    val totalPrice: Double
)

