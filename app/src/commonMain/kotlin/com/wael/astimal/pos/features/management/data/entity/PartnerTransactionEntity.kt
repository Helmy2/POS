package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.core.util.Clock
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.opening_balance
import pos.app.generated.resources.payment_received
import pos.app.generated.resources.payment_sent
import pos.app.generated.resources.purchase
import pos.app.generated.resources.purchase_return
import pos.app.generated.resources.sale
import pos.app.generated.resources.sale_return


@Entity(
    tableName = "partner_transactions",
    indices = [Index("partnerLocalId")]
)
data class PartnerTransactionEntity(
    @PrimaryKey(autoGenerate = true) override val localId: Long = 0L,
    override val serverId: Long?,
    override var isSynced: Boolean = false,
    override val createdAt: Long = Clock.now(),
    override val updatedAt: Long = Clock.now(),
    override var isDeletedLocally: Boolean = false,
    val partnerLocalId: Long?,
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

    fun getStringRes(type: TransactionType = this): StringResource {
        return when (type) {
            OPENING_BALANCE -> Res.string.opening_balance
            SALE -> Res.string.sale
            PURCHASE -> Res.string.purchase
            SALE_RETURN -> Res.string.sale_return
            PURCHASE_RETURN -> Res.string.purchase_return
            PAYMENT_RECEIVED -> Res.string.payment_received
            PAYMENT_SENT -> Res.string.payment_sent
        }
    }
}