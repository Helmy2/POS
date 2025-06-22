package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.data.entity.ItemEntity

@Entity(
    tableName = "partner_transactions",
    indices = [Index("clientId"), Index("supplierId")]
)
data class PartnerTransactionEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = System.currentTimeMillis(),
    override val updatedAt: Long = System.currentTimeMillis(),
    override var isDeletedLocally: Boolean = false,
    val clientId: Long?,
    val supplierId: Long?,
    val sourceTransactionId: Long,
    val transactionType: TransactionType,
    val debit: Double = 0.0,
    val credit: Double = 0.0
) : ItemEntity


enum class TransactionType {
    OPENING_BALANCE,
    SALE,
    PURCHASE,
    SALE_RETURN,
    PURCHASE_RETURN,
    PAYMENT_RECEIVED,
    PAYMENT_SENT;

    fun getStringRes(type: TransactionType = this): Int {
        return when (type) {
            OPENING_BALANCE -> R.string.opening_balance
            SALE -> R.string.sale
            PURCHASE -> R.string.purchase
            SALE_RETURN -> R.string.sale_return
            PURCHASE_RETURN -> R.string.purchase_return
            PAYMENT_RECEIVED -> R.string.payment_received
            PAYMENT_SENT -> R.string.payment_sent
        }
    }
}