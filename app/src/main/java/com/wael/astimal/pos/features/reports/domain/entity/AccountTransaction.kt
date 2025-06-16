package com.wael.astimal.pos.features.reports.domain.entity

import java.time.LocalDateTime

/**
 * Represents a single, unified transaction in a business partner's account statement.
 * This class is used to combine different types of financial records (sales, purchases, payments, etc.)
 * into a single chronological list.
 *
 * @property date The exact date and time the transaction occurred.
 * @property transactionId A unique identifier for the transaction (e.g., invoice number, voucher ID).
 * @property description A human-readable description of the transaction.
 * @property transactionType The type of transaction, used for display logic and grouping.
 * @property debit The amount that increases the balance (money the partner owes you).
 * @property credit The amount that decreases the balance (money you owe the partner or payments they made).
 * @property balance The running balance of the account after this transaction has been applied.
 */
data class AccountTransaction(
    val date: LocalDateTime,
    val transactionId: String,
    val description: String,
    val transactionType: TransactionType,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0
)

/**
 * Defines the different kinds of financial transactions that can appear on a statement.
 * This helps in styling and interpreting the transaction data in the UI.
 */
enum class TransactionType {
    OPENING_BALANCE,
    SALE,
    PURCHASE,
    SALE_RETURN,
    PURCHASE_RETURN,
    PAYMENT_RECEIVED,
    PAYMENT_SENT
}
