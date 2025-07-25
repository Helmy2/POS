package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import kotlinx.coroutines.flow.Flow

interface ClientDebitRepository {
    fun getClientsWithDebit(
        responsibleEmployeeId: String? // Nullable for "All Employees"
    ): Flow<List<ClientDebitInfo>>
}