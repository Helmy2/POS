package com.wael.astimal.pos.features.management.data.remote.dto

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.parseIsoTimestamp
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class PartnerTransactionDto(
    val id: String,
    val balance: Double,
    val notes: String?,
    @SerialName("created_at")
    val createdAt: String,
    @SerialName("updated_at")
    val updatedAt: String,
    @SerialName("transaction_type")
    val transactionType: String,
    @SerialName("invoice_id")
    val invoiceId: String?,
    @SerialName("business_partner_id")
    val partnerId: Long,
    @SerialName("created_by_user_id")
    val createdByUserId: String
)

fun PartnerTransactionDto.toEntity(
    partnerLocalId: Long, userLocalId: Long
): PartnerTransactionEntity {
    return PartnerTransactionEntity(
        serverId = id,
        partnerLocalId = partnerLocalId,
        invoiceId = invoiceId,
        transactionType = TransactionType.valueOf(this.transactionType.uppercase()),
        balance = balance,
        notes = notes,
        createdAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        updatedAt = createdAt.parseIsoTimestamp() ?: Clock.now(),
        isSynced = true,
        employeeLocalId = userLocalId,
    )
}