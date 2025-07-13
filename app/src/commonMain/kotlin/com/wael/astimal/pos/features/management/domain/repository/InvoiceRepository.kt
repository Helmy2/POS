package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.Invoice
import kotlinx.coroutines.flow.Flow


interface InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>>
    suspend fun addSalesOrder(invoice: Invoice): Result<Unit>
    suspend fun updateOrder(invoice: Invoice): Result<Unit>
    suspend fun deleteSalesOrder(orderId: String): Result<Unit>

    // In the future, you would add methods like:
    // suspend fun updateSalesOrder(salesOrder: SalesOrder): Result<Unit>
    // suspend fun pushUnsyncedOrders(): Result<Unit>
}