package com.wael.astimal.pos.features.management.data.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey
import com.wael.astimal.pos.core.data.entity.ItemEntity
import com.wael.astimal.pos.features.reports.domain.entity.TransactionType

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
