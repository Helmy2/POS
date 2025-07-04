package com.wael.astimal.pos.core.util

import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner

actual class PdfGeneratorImpl : PdfGenerator {
    override fun generateStatementPdf(
        partner: BusinessPartner,
        transactions: List<AccountTransaction>
    ) {
        TODO("Not yet implemented")
    }
}