package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.core.util.Clock
import com.wael.astimal.pos.features.management.data.entity.ReceivePayVoucherEntity
import com.wael.astimal.pos.features.user.domain.entity.User
import org.jetbrains.compose.resources.StringResource
import pos.app.generated.resources.Res
import pos.app.generated.resources.pay_to_supplier
import pos.app.generated.resources.receive_from_client

enum class VoucherPartyType {
    CLIENT,
    SUPPLIER;

    fun getStringRes(type: VoucherPartyType = this): StringResource {
        return when (type) {
            CLIENT -> Res.string.receive_from_client
            SUPPLIER -> Res.string.pay_to_supplier
        }
    }
}

data class ReceivePayVoucher(
    val party: BusinessPartner,
    val amount: Double,
    val partyType: VoucherPartyType,
    val notes: String,
    val createdBy: User,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long = Clock.now(),
    override val isSynced: Boolean = false,
) : Item

fun ReceivePayVoucher.matchesQuery(query: String): Boolean {
    if (query.isBlank()) return true
    val queryLower = query.lowercase()
    return party.name.contains(queryLower) ||
            notes.contains(queryLower, ignoreCase = true) ||
            amount.toString().contains(queryLower)
}

fun ReceivePayVoucher.toEntity() = ReceivePayVoucherEntity(
    localId = id.local,
    serverId = id.server,
    partyType = partyType,
    amount = amount,
    notes = notes,
    createdAt = createdAt,
    updatedAt = updatedAt,
    isSynced = isSynced,
    partnerLocalId = party.id.local,
    employeeLocalId = createdBy.id,
)