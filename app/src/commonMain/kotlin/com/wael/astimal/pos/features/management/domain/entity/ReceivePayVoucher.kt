package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.management.data.local.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.data.remote.dto.PartnerTransactionDto
import com.wael.astimal.pos.features.user.domain.entity.User


data class ReceivePayVoucher(
    val partner: BusinessPartner,
    val amount: Double,
    val notes: String,
    val createdBy: User,
    val invoiceId: String?,
    val transactionType: TransactionType,
    val id: String,
    val createdAt: Long,
    val updatedAt: Long = Clock.now(),
    val isSynced: Boolean = false,
)

fun ReceivePayVoucher.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return partner.name.contains(queryLower) || notes.contains(
        queryLower,
        ignoreCase = true
    ) || amount.toString().contains(queryLower)
}

fun ReceivePayVoucher.toEntity() = PartnerTransactionEntity(
    localId = id,
    balance = amount,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    partnerLocalId = partner.id,
    employeeLocalId = createdBy.id,
    invoiceId = invoiceId,
    transactionType = transactionType
)


fun ReceivePayVoucher.toDto(updatedAt: String): PartnerTransactionDto {
    return PartnerTransactionDto(
        id = id,
        transactionType = transactionType.name.lowercase(),
        balance = amount,
        notes = notes,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt,
        createdByUserId = createdBy.id,
        invoiceId = invoiceId.takeIf { it?.isNotBlank() == true },
        partnerId = partner.id
    )
}