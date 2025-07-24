package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.core.domain.entity.LocalizedString
import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import kotlinx.datetime.LocalDate


data class DetailedTransaction(
    val id: String,
    val date: LocalDate,
    val transactionType: TransactionType,
    val invoiceId: String,
    val totalAmount: Double,
    val partnerName: LocalizedString,
)