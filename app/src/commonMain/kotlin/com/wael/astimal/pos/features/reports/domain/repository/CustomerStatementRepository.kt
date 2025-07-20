package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import kotlinx.coroutines.flow.Flow
import kotlinx.datetime.LocalDate

interface CustomerStatementRepository {
    fun getTransactionsForPartner(
        partnerId: String,
        startDate: LocalDate,
        endDate: LocalDate
    ): Flow<List<DetailedTransaction>>
}