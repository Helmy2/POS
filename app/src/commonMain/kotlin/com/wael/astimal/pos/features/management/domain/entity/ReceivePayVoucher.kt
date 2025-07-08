package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.core.util.toDateString
import com.wael.astimal.pos.features.management.data.entity.PartnerTransactionEntity
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.data.remote.dto.PartnerTransactionDto
import com.wael.astimal.pos.features.user.domain.entity.User


data class ReceivePayVoucher(
    val partner: BusinessPartner,
    val amount: Double,
    val notes: String,
    val createdBy: User,
    val invoiceId: String?,
    val transactionType: TransactionType,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

fun ReceivePayVoucher.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return partner.name.contains(queryLower) || notes.contains(
        queryLower,
        ignoreCase = true
    ) || amount.toString().contains(queryLower)
}

fun ReceivePayVoucher.toEntity() = PartnerTransactionEntity(
    localId = id.local,
    serverId = id.serverStringId,
    balance = amount,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    partnerLocalId = partner.id.local,
    employeeLocalId = createdBy.id.local,
    invoiceId = invoiceId,
    transactionType = transactionType
)


fun ReceivePayVoucher.toDto(): PartnerTransactionDto {
    return PartnerTransactionDto(
        id = id.serverStringId ?: "",
        transactionType = transactionType.name.lowercase(),
        balance = amount,
        notes = notes,
        createdAt = createdAt.toDateString(),
        updatedAt = updatedAt.toDateString(),
        createdByUserId = createdBy.id.serverStringId!!,
        invoiceId = invoiceId.takeIf { it?.isNotBlank() == true },
        partnerId = partner.id.server!!
    )
}