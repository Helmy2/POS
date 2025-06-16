package com.wael.astimal.pos.features.reports.domain.repository

import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.entity.AccountTransaction
import kotlinx.coroutines.flow.Flow

/**
 * Defines the contract for fetching a complete, unified account statement for a BusinessPartner.
 */
interface AccountStatementRepository {

    /**
     * Retrieves a chronological list of all financial transactions for a given business partner.
     * The implementation will be responsible for fetching data from various sources (sales, purchases,
     * vouchers, etc.), mapping them to the unified [AccountTransaction] model, and calculating a
     * running balance.
     *
     * @param partner The business partner for whom to generate the statement. The repository will use
     * the clientLocalId and supplierLocalId from this object to fetch the relevant data.
     * @return A Flow emitting a list of [AccountTransaction] objects, sorted by date.
     */
    fun getAccountStatement(partner: BusinessPartner): Flow<List<AccountTransaction>>
}
