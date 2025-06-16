package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType

/**
 * Represents a single, immutable financial transaction in the ledger for both clients and suppliers.
 * This is the single source of truth for all financial movements.
 *
 * @param id The unique identifier for this transaction.
 * @param clientId The ID of the client this transaction is for, if applicable.
 * @param supplierId The ID of the supplier this transaction is for, if applicable.
 * @param sourceTransactionId The ID of the original record that generated this ledger entry (e.g., the Sales Order ID, the Voucher ID).
 * @param transactionType The type of transaction (e.g., SALE, PURCHASE, PAYMENT_RECEIVED).
 * @param date The timestamp when the transaction occurred.
 * @param debit The amount that increases the balance owed by the partner (They owe you more).
 * @param credit The amount that decreases the balance owed by the partner (You owe them more, or they paid you).
 */
@Entity(
    tableName = "partner_transactions",
    indices = [Index("clientId"), Index("supplierId")]
)
data class PartnerTransactionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0L,
    val clientId: Long?,
    val supplierId: Long?,
    val sourceTransactionId: Long,
    val transactionType: TransactionType,
    val date: Long,
    val debit: Double = 0.0,
    val credit: Double = 0.0
)
