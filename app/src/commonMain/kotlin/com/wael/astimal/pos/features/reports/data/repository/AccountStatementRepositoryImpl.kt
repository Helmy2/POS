package com.wael.astimal.pos.features.reports.data.repository

import com.wael.astimal.pos.core.util.toLocalDateTime
import com.wael.astimal.pos.features.management.data.local.dao.PartnerTransactionDao
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
            partner.id
        )

        // Combine the live ledger data with the static partner data to build the statement.
        return ledgerFlow.combine(flowOf(partner)) { ledgerEntries, currentPartner ->
            val statement = mutableListOf<AccountTransaction>()

            var runningBalance = 0.0
            ledgerEntries.forEach { entry ->
                runningBalance += entry.balance
                statement.add(
                    AccountTransaction(
                        date = entry.createdAt.toLocalDateTime(),
                        transactionId = "${entry.transactionType}-${entry.localId}",
                        invoiceNumber = "Ref #${entry.invoiceId}", // This can be enhanced later if needed
                        transactionType = entry.transactionType,
                        debit = if (entry.balance > 0) entry.balance else 0.0,
                        credit = if (entry.balance < 0) -entry.balance else 0.0,
                        balance = runningBalance
                    )
                )
            }
            statement
        }
    }
}
