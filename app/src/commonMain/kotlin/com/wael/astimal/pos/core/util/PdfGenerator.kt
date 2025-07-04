package com.wael.astimal.pos.core.util

import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner

expect class PdfGeneratorImpl : PdfGenerator


interface PdfGenerator {
    fun generateStatementPdf(partner: BusinessPartner, transactions: List<AccountTransaction>)
}