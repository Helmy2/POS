package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.R
import com.wael.astimal.pos.core.domain.entity.Id
import com.wael.astimal.pos.core.domain.entity.Item
import com.wael.astimal.pos.features.user.domain.entity.User

enum class VoucherPartyType {
    CLIENT,
    SUPPLIER;

    fun getStringRes(type: VoucherPartyType = this): Int {
        return when (type) {
            CLIENT -> R.string.receive_from_client
            SUPPLIER -> R.string.pay_to_supplier
        }
    }
}

data class ReceivePayVoucher(
    val amount: Double,
    val party: Any,
    val partyType: VoucherPartyType,
    val notes: String,
    val createdBy: User,
    override val id: Id,
    override val createdAt: Long,
    override val updatedAt: Long,
    override val isSynced: Boolean,
) : Item