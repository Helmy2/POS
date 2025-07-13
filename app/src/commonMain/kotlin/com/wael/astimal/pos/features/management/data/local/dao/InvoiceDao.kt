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

    /**
     * Inserts an invoice header and returns its newly generated localId.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoice(invoice: InvoiceEntity)

    /**
     * Inserts a list of invoice items.
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertInvoiceItems(items: List<InvoiceItemEntity>)

    /**
     * A transactional function that inserts an invoice and its items together,
     * ensuring data integrity.
     */
    @Transaction
    suspend fun insertInvoiceWithItems(
        invoice: InvoiceEntity,
        items: List<InvoiceItemEntity>
    ) {
        insertInvoice(invoice)
        val itemsWithInvoiceId = items.map { it.copy(supabaseId = invoice.supabaseId) }
        insertInvoiceItems(itemsWithInvoiceId)
    }

    @Transaction
    @Query("SELECT * FROM invoices WHERE isDeletedLocally = 0")
    fun getAllInvoicesWithItems(): Flow<List<InvoiceWithItems>>

    @Transaction
    @Query("SELECT * FROM invoices WHERE isSynced = 0 AND isDeletedLocally = 0")
    suspend fun getUnsyncedInvoices(): List<InvoiceWithItems>
}