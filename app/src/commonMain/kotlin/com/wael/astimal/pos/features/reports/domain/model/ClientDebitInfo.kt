package com.wael.astimal.pos.features.reports.domain.model

import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner

/**
 * Represents the debit information for a single client.
 */
data class ClientDebitInfo(
    val client: BusinessPartner,
    val debitAmount: Double
)
