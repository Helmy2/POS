package com.wael.astimal.pos.features.management.domain.entity

import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import java.time.LocalDateTime

data class AccountTransaction(
    val date: LocalDateTime,
    val transactionId: String,
    val transactionType: TransactionType,
    val invoiceNumber: String,
    val debit: Double = 0.0,
    val credit: Double = 0.0,
    val balance: Double = 0.0
)