package com.wael.astimal.pos.features.management.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Transaction
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceItemEntity
import com.wael.astimal.pos.features.management.data.local.entity.InvoiceWithItems
import kotlinx.coroutines.flow.Flow


@Dao
interface InvoiceDao {


    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    @Transaction
    suspend fun insertInvoiceWithItems(
        invoice: InvoiceEntity, items: List<InvoiceItemEntity>
    ) {
        insertInvoice(invoice)
        val itemsWithInvoiceId = items.map { it.copy(supabaseId = invoice.supabaseId) }
        insertInvoiceItems(itemsWithInvoiceId)
    }

    @Transaction
    suspend fun deleteInvoiceWithItemsById(id: String) {
        deleteInvoiceItemsByInvoiceId(id)
        deleteInvoiceById(id)
    }

    @Query("DELETE FROM invoices WHERE supabaseId = :id")
    suspend fun deleteInvoiceById(id: String)

    @Query("DELETE FROM invoice_items WHERE invoiceId = :id")
    suspend fun deleteInvoiceItemsByInvoiceId(id: String)

    @Transaction
    @Query("SELECT * FROM invoices WHERE isDeletedLocally = 0")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE  isDeletedLocally = 0")
    suspend fun getUnsyncedInvoices(): List<InvoiceWithItems>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isDeletedLocally = 1")
    suspend fun getDeletedInvoice(): List<InvoiceWithItems>

    @Query("DELETE FROM invoices WHERE supabaseId = :id")
    suspend fun hardDeleteInvoiceById(id: String)


    @Query("SELECT * FROM invoice_items WHERE isDeletedLocally = 0")
    suspend fun getUnsyncedInvoicesItems(): List<InvoiceItemEntity>

    @Query("SELECT * FROM invoice_items WHERE isDeletedLocally = 1")
    suspend fun getDeletedInvoiceItems(): List<InvoiceItemEntity>

    @Query("DELETE FROM invoice_items WHERE supabaseId = :id")
    suspend fun hardDeleteInvoiceItemsById(id: String)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceInvoice(item: InvoiceItemEntity)
}