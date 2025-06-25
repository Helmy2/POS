package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.util.toLocalDateTime
import com.wael.astimal.pos.features.management.data.local.PartnerTransactionDao
import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.repository.AccountStatementRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flowOf


class AccountStatementRepositoryImpl(
    private val partnerTransactionDao: PartnerTransactionDao
) : AccountStatementRepository {

    override fun getAccountStatement(partner: BusinessPartner): Flow<List<AccountTransaction>> {
        // Determine which ledger query to use based on the partner type.
        val ledgerFlow = partnerTransactionDao.getTransactionsForPartner(
            partner.id.local
        )

        // Combine the live ledger data with the static partner data to build the statement.
        return ledgerFlow.combine(flowOf(partner)) { ledgerEntries, currentPartner ->
            val statement = mutableListOf<AccountTransaction>()

            var runningBalance = 0.0
            ledgerEntries.forEach { entry ->
                runningBalance += (entry.debit - entry.credit)
                statement.add(
                    AccountTransaction(
                        date = entry.createdAt.toLocalDateTime(),
                        transactionId = "${entry.transactionType}-${entry.sourceTransactionId}",
                        invoiceNumber = "Ref #${entry.sourceTransactionId}", // This can be enhanced later if needed
                        transactionType = entry.transactionType,
                        debit = entry.debit,
                        credit = entry.credit,
                        balance = runningBalance
                    )
                )
            }
            statement
        }
    }
}
