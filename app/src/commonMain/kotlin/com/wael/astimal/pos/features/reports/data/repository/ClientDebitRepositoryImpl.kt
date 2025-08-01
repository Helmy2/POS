package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.domain.repository.BusinessPartnerRepository
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.reports.domain.repository.ClientDebitRepository
import com.wael.astimal.pos.features.user.domain.repository.UserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientDebitRepositoryImpl(
    val userRepository: UserRepository,
    val businessPartnerRepository: BusinessPartnerRepository,
    val db: AppDatabase
) : ClientDebitRepository {

    override suspend fun getClientsWithDebit(
        responsibleEmployeeId: String?
    ): Flow<List<ClientDebitInfo>> {
        return businessPartnerRepository
            .getBusinessPartners("")
            .map { allPartners ->
                allPartners
                    .filter { responsibleEmployeeId == null || it.responsibleEmployee.id == responsibleEmployeeId }
                    .map {
                        val balance =
                            db.partnerTransactionDao().getPartnerBalance(it.id)
                                ?: 0.0
                        ClientDebitInfo(
                            client = it,
                            debitAmount = balance
                        )
                    }.filter { it.debitAmount > 0.0 }
            }
    }
}