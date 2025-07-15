package com.wael.astimal.pos.features.management.domain.repository

import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import com.wael.astimal.pos.features.management.domain.entity.Invoice
import kotlinx.coroutines.flow.Flow


interface InvoiceRepository {
    fun getInvoices(): Flow<List<Invoice>>
    suspend fun addSalesOrder(invoice: Invoice): Result<Unit>
    suspend fun updateOrder(invoice: Invoice): Result<Unit>
    suspend fun deleteSalesOrder(orderId: String): Result<Unit>

    suspend fun syncInvoices(entities: List<InvoiceEntity>): Result<Unit>
    suspend fun hardDeleteInvoice(id: String): Result<Unit>
    suspend fun getAllDeletedInvoice(): Result<List<Invoice>>
    suspend fun getUnsyncedInvoices(): Result<List<Invoice>>
    suspend fun getUnsyncedInvoicesItems(): Result<List<InvoiceItemEntity>>
    suspend fun getAllDeletedInvoiceItems(): Result<List<InvoiceItemEntity>>
    suspend fun hardDeleteInvoiceItems(id: String): Result<Unit>
    suspend fun syncInvoicesItems(entities: List<InvoiceItemEntity>): Result<Unit>
}