package com.wael.astimal.pos.features.management.domain.entity

data class EditableItemList(
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
