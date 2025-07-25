package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.data.AppDatabase
import com.wael.astimal.pos.features.management.data.local.entity.toDomain
import com.wael.astimal.pos.features.management.domain.entity.PartnerType
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.reports.domain.repository.ClientDebitRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

class ClientDebitRepositoryImpl(
    private val db: AppDatabase
) : ClientDebitRepository {

    override fun getClientsWithDebit(
        responsibleEmployeeId: String?
    ): Flow<List<ClientDebitInfo>> {
        return db.businessPartnerDao().searchPartnersFlow("")
            .map { allPartners ->
                allPartners
                    .filter { it.businessPartner.type != PartnerType.SUPPLIER }
                    .filter { responsibleEmployeeId == null || it.businessPartner.responsibleEmployeeLocalId == responsibleEmployeeId }
                    .map {
                        val balance =
                            db.partnerTransactionDao().getPartnerBalance(it.businessPartner.localId)
                                ?: 0.0
                        ClientDebitInfo(
                            client = it.toDomain(),
                            debitAmount = -balance
                        )
                    }.filter { it.debitAmount >= 0.0 }
            }
    }
}