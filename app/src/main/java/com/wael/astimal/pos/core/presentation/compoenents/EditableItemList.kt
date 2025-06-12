package com.wael.astimal.pos.core.presentation.compoenents

import com.wael.astimal.pos.features.management.domain.entity.Client
import com.wael.astimal.pos.features.management.domain.entity.EditableItem
import com.wael.astimal.pos.features.management.domain.entity.PaymentType

data class EditableItemList(
    val selectedClient: Client? = null,
    val selectedEmployeeId: Long? = null,
    val paymentType: PaymentType = PaymentType.CASH,
    val date: Long = System.currentTimeMillis(),
    val items: List<EditableItem> = listOf(),
    val amountPaid: String = "0.0",
) {
    val totalAmount: Double get() = items.sumOf { it.lineTotal }
    val amountRemaining: Double get() {
        val paidAmount = amountPaid.toDoubleOrNull() ?: 0.0
        return totalAmount - paidAmount
    }
}