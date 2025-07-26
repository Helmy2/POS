package com.wael.astimal.pos.core.util


import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.model.ClientDebitInfo
import com.wael.astimal.pos.features.reports.domain.model.CurrentStockInfo
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.model.EmployeeLedgerEntry
import com.wael.astimal.pos.features.reports.domain.model.ProductMovementGroup
import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.compose.resources.getString
import pos.app.generated.resources.Res
import pos.app.generated.resources.account_statement
import pos.app.generated.resources.amount
import pos.app.generated.resources.balance
import pos.app.generated.resources.date
import pos.app.generated.resources.description
import pos.app.generated.resources.details
import pos.app.generated.resources.employee_activity_report
import pos.app.generated.resources.invoice_details_format
import pos.app.generated.resources.partner_payment
import pos.app.generated.resources.partner_payment_details_format
import pos.app.generated.resources.total
import pos.app.generated.resources.type
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a self-contained HTML string for reports.
 * This is the single source of truth for the report's layout and styling.
 */
class HtmlReportGenerator(
    val settingsManager: SettingsManager,
) {

    private data class ReportRowData(
        val date: String,
        val type: String,
        val details: String,
        val amount: String,
    )

    private data class CustomerStatementRowData(
        val date: String,
        val description: String,
        val balance: String,
    )

    private data class LedgerReportRowData(
        val date: String,
        val description: String,
        val debit: String,
        val credit: String,
        val balance: String,
    )

    private fun formatDate(date: LocalDate, lang: Language = Language.English): String {
        val sdf = SimpleDateFormat(
            "dd MMM yyyy",
            if (lang == Language.Arabic) Locale.forLanguageTag("ar") else Locale.US
        )
        // Convert LocalDate to a format SimpleDateFormat can use
        val dateInMillis = date.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        return sdf.format(Date(dateInMillis))
    }

    suspend fun createClientDebitReportHtml(
        debitList: List<ClientDebitInfo>
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val totalDebit = debitList.sumOf { it.debitAmount }

        val debitRows = debitList.joinToString("") { info ->
            """
        <tr>
            <td>${if (isRtl) info.client.name.arName else info.client.name.enName}</td>
            <td>${info.client.phone ?: ""}</td>
            <td>${if (isRtl) info.client.responsibleEmployee.localizedName.arName else info.client.responsibleEmployee.localizedName.enName}</td>
            <td class="num">${String.format("%.2f", info.debitAmount)}</td>
        </tr>
        """.trimIndent()
        }

        val title = if (isRtl) "تقرير مديونية العملاء" else "Client Debit Report"
        val headers = if (isRtl) {
            listOf("اسم العميل", "رقم الهاتف", "الموظف المسؤول", "قيمة المديونية")
        } else {
            listOf("Client Name", "Phone Number", "Responsible Employee", "Debit Amount")
        }

        val totalLabel = if (isRtl) "إجمالي المديونيات" else "Total Debits"

        val footer = """
        <tfoot>
            <tr class="total-row">
                <td colspan="3" style="text-align: ${if (isRtl) "left" else "right"};">$totalLabel</td>
                <td class="num">${String.format("%.2f", totalDebit)}</td>
            </tr>
        </tfoot>
    """.trimIndent()

        val html = generateHtmlShell(
            title = title,
            subtitle = "",
            dateRange = "",
            isRtl = isRtl,
            headers = headers,
            rows = debitRows,
            footer = footer
        )

        return html
    }

    suspend fun createCurrentStockReportHtml(
        stockList: List<CurrentStockInfo>
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val totalValue = stockList.sumOf { it.quantity * it.product.averagePrice }

        val stockRows = stockList.joinToString("") { info ->
            """
        <tr>
            <td>${if (isRtl) info.product.name.arName else info.product.name.enName}</td>
            <td>${if (isRtl) info.store.name.arName else info.store.name.enName}</td>
            <td class="num">${String.format("%.2f", info.quantity)}</td>
        </tr>
        """.trimIndent()
        }

        val title = if (isRtl) "تقرير المخزون الحالي" else "Current Stock Report"
        val headers = if (isRtl) {
            listOf("اسم الصنف", "اسم المخزن", "الكمية الحالية")
        } else {
            listOf("Product Name", "Store Name", "Current Quantity")
        }


        val html = generateHtmlShell(
            title = title,
            subtitle = "", // No subtitle for this report
            dateRange = "", // No date range for this report
            isRtl = isRtl,
            headers = headers,
            rows = stockRows,
        )

        return html
    }

    suspend fun createProductMovementReportHtml(
        groups: List<ProductMovementGroup>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val productTables = groups.joinToString("") { group ->
            val entryRows = group.entries.joinToString("") { entry ->
                val description: String = buildString {
                    val productName =
                        if (isRtl) entry.productName.arName else entry.productName.enName
                    val storeName = if (isRtl) entry.storeName.arName else entry.storeName.enName
                    append("$productName ($storeName)")
                }

                """
            <tr>
                <td>${formatDate(entry.date, lang)}</td>
                <td>$description</td>
                <td class="num">${
                    if (entry.quantityIn > 0) String.format(
                        "%.2f",
                        entry.quantityIn
                    ) else ""
                }</td>
                <td class="num">${
                    if (entry.quantityOut > 0) String.format(
                        "%.2f",
                        entry.quantityOut
                    ) else ""
                }</td>
                <td class="num">${String.format("%.2f", entry.balance)}</td>
            </tr>
            """.trimIndent()
            }

            val headers = if (isRtl) {
                listOf("التاريخ", "الوصف", "داخل", "خارج", "الرصيد")
            } else {
                listOf("Date", "Description", "In", "Out", "Balance")
            }
            val headerRow = headers.joinToString("") { "<th>$it</th>" }

            val totalsLabel = if (isRtl) "الإجماليات" else "Totals"
            val closingBalanceLabel = if (isRtl) "الرصيد الختامي" else "Closing Balance"
            val productName = if (isRtl) group.productName.arName else group.productName.enName

            """
        <div class="product-group">
            <h3>${productName}</h3>
            <table>
                <thead><tr>$headerRow</tr></thead>
                <tbody>$entryRows</tbody>
                <tfoot>
                    <tr class="total-row">
                        <td colspan="2">$totalsLabel</td>
                        <td class="num">${String.format("%.2f", group.totalIn)}</td>
                        <td class="num">${String.format("%.2f", group.totalOut)}</td>
                        <td></td>
                    </tr>
                    <tr class="total-row">
                        <td colspan="4" style="text-align: ${if (isRtl) "left" else "right"};">$closingBalanceLabel</td>
                        <td class="num">${String.format("%.2f", group.closingBalance)}</td>
                    </tr>
                </tfoot>
            </table>
        </div>
        """.trimIndent()
        }

        val dateRangeText = if (formatDate(startDate, lang) == formatDate(endDate, lang)) {
            if (isRtl) "تقرير لتاريخ: ${
                formatDate(
                    startDate,
                    lang
                )
            }" else "Report for date: ${formatDate(startDate, lang)}"
        } else {
            if (isRtl) "تقرير للفترة: ${formatDate(startDate, lang)} إلى ${
                formatDate(
                    endDate,
                    lang
                )
            }" else "Report for period: ${formatDate(startDate, lang)} to ${
                formatDate(
                    endDate,
                    lang
                )
            }"
        }

        val title = if (isRtl) "تقرير حركة صنف" else "Product Movement Report"

        val html = generateHtmlShell(
            title = title,
            subtitle = "", // No subtitle for this report
            dateRange = dateRangeText,
            isRtl = isRtl,
            rows = productTables,
        )

        return html
    }

    suspend fun createCustomerStatementHtml(
        partner: BusinessPartner,
        transactions: List<DetailedTransaction>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val processedRows = transactions.map { trx ->
            CustomerStatementRowData(
                date = formatDate(trx.date, lang),
                description = getString(trx.transactionType.getStringRes()) + " " + getString(
                    Res.string.invoice_details_format,
                    trx.id,
                    if (isRtl) partner.name.arName ?: "" else partner.name.enName ?: ""
                ),
                balance = String.format("%.2f", trx.totalAmount)
            )
        }

        val activityRows = processedRows.joinToString("") { row ->
            """
            <tr>
                <td>${row.date}</td>
                <td>${row.description}</td>
                <td class="num">${row.balance}</td>
            </tr>
            """.trimIndent()
        }

        val formattedStartDate = formatDate(startDate, lang)
        val formattedEndDate = formatDate(endDate, lang)
        val dateRangeText = if (formattedStartDate == formattedEndDate) {
            if (isRtl) "تقرير لتاريخ: $formattedStartDate" else "Report for date: $formattedStartDate"
        } else {
            if (isRtl) "تقرير للفترة: $formattedStartDate إلى $formattedEndDate" else "Report for period: $formattedStartDate to $formattedEndDate"
        }

        val title = if (isRtl) getString(Res.string.account_statement) else "Account Statement"
        val subtitle = if (isRtl) partner.name.arName ?: "" else partner.name.enName ?: ""
        val headers = if (isRtl) {
            listOf(
                getString(Res.string.date),
                getString(Res.string.description),
                getString(Res.string.balance)
            )
        } else {
            listOf("Date", "Description", "Balance")
        }

        val footer =
            """
            <tr>
                <td colspan="2">${getString(Res.string.total)}</td>
                <td class="num">${String.format("%.2f", transactions.sumOf { it.totalAmount })}</td>
            </tr>
            """.trimIndent()

        val html = generateHtmlShell(
            title = title,
            subtitle = subtitle,
            dateRange = dateRangeText,
            isRtl = isRtl,
            headers = headers,
            rows = activityRows,
            footer = footer
        )

        return html
    }


    suspend fun createEmployeeActivityReportHtml(
        employee: User,
        activities: List<EmployeeActivity>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val processedDataJobs = activities.map { activity ->
            val date = activity.timestamp.toLocalDateTime(TimeZone.UTC).date
            val typeString: String
            val detailsString: String
            val amount: Double

            when (activity) {
                is EmployeeActivity.InvoiceActivity -> {
                    typeString = getString(activity.invoice.invoiceType.getStringResId())
                    val partnerName =
                        if (isRtl) activity.invoice.partner.name.arName else activity.invoice.partner.name.enName
                    detailsString = getString(
                        Res.string.invoice_details_format,
                        activity.invoice.id,
                        partnerName ?: ""
                    )
                    amount = activity.invoice.totalAmount
                }

                is EmployeeActivity.FinancialActivity -> {
                    typeString = getString(activity.transaction.type.getStringResId())
                    detailsString =
                        activity.transaction.notes.takeIf { it?.isNotBlank() == true } ?: typeString
                    amount = activity.transaction.amount
                }

                is EmployeeActivity.PartnerPaymentActivity -> {
                    typeString = getString(Res.string.partner_payment)
                    val partnerName =
                        if (isRtl) activity.transaction.partner.name.arName else activity.transaction.partner.name.enName
                    detailsString = getString(
                        Res.string.partner_payment_details_format,
                        partnerName ?: ""
                    )
                    amount = activity.transaction.amount
                }
            }

            ReportRowData(
                date = formatDate(date, lang),
                type = typeString,
                details = detailsString,
                amount = String.format("%.2f", amount)
            )
        }

        val activityRows = processedDataJobs.joinToString("") { row ->
            """
            <tr>
                <td>${row.date}</td>
                <td>${row.type}</td>
                <td>${row.details}</td>
                <td class="num">${row.amount}</td>
            </tr>
            """.trimIndent()
        }

        val formattedStartDate = formatDate(startDate, lang)
        val formattedEndDate = formatDate(endDate, lang)
        val dateRangeText = if (formattedStartDate == formattedEndDate) {
            if (isRtl) "تقرير لتاريخ: $formattedStartDate" else "Report for date: $formattedStartDate"
        } else {
            if (isRtl) "تقرير للفترة: $formattedStartDate إلى $formattedEndDate" else "Report for period: $formattedStartDate to $formattedEndDate"
        }

        val title =
            if (isRtl) getString(Res.string.employee_activity_report) else "Employee Activity Report"
        val subtitle = if (isRtl) employee.localizedName.arName
            ?: employee.name else employee.localizedName.enName ?: employee.name
        val headers = if (isRtl) {
            listOf(
                getString(Res.string.date),
                getString(Res.string.type),
                getString(Res.string.details),
                getString(Res.string.amount)
            )
        } else {
            listOf("Date", "Type", "Details", "Amount")
        }

        return generateHtmlShell(
            title = title,
            subtitle = subtitle,
            dateRange = dateRangeText,
            isRtl = isRtl,
            headers = headers,
            rows = activityRows
        )
    }

    suspend fun createEmployeeLedgerHtml(
        employee: User,
        entries: List<EmployeeLedgerEntry>,
        startDate: LocalDate,
        endDate: LocalDate,
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val processedDataJobs = entries.map { entry ->
            val description = getString(entry.transactionType.getStringResId())
            LedgerReportRowData(
                date = formatDate(entry.date, lang),
                description = description,
                debit = if (entry.debit > 0) String.format("%.2f", entry.debit) else "",
                credit = if (entry.credit > 0) String.format("%.2f", entry.credit) else "",
                balance = String.format("%.2f", entry.balance)
            )
        }


        val entryRows = processedDataJobs.joinToString("") { row ->
            """
            <tr>
                <td>${row.date}</td>
                <td>${row.description}</td>
                <td class="num">${row.debit}</td>
                <td class="num">${row.credit}</td>
                <td class="num">${row.balance}</td>
            </tr>
            """.trimIndent()
        }

        val totalDebit = entries.sumOf { it.debit }
        val totalCredit = entries.sumOf { it.credit }
        val closingBalance = entries.lastOrNull()?.balance ?: 0.0

        val formattedStartDate = formatDate(startDate, lang)
        val formattedEndDate = formatDate(endDate, lang)
        val dateRangeText = if (formattedStartDate == formattedEndDate) {
            if (isRtl) "تقرير لتاريخ: ${formattedStartDate}" else "Report for date: ${formattedStartDate}"
        } else {
            if (isRtl) "تقرير للفترة: ${formattedStartDate} إلى ${formattedEndDate}" else "Report for period: ${formattedStartDate} to ${formattedEndDate}"
        }

        val title = if (isRtl) "كشف حساب الموظف" else "Employee Ledger"
        val subtitle = if (isRtl) employee.localizedName.arName
            ?: employee.name else employee.localizedName.enName ?: employee.name
        val headers = if (isRtl) {
            listOf("التاريخ", "الوصف", "عليه", "له", "الرصيد")
        } else {
            listOf("Date", "Description", "Owed", "Paid", "Balance")
        }

        val totalsLabel = if (isRtl) "الإجماليات" else "Totals"
        val closingBalanceLabel = if (isRtl) "الرصيد الختامي" else "Closing Balance"

        val footer = """
    <tfoot>
        <tr class="total-row">
            <td colspan="2">$totalsLabel</td>
            <td class="num">${String.format("%.2f", totalDebit)}</td>
            <td class="num">${String.format("%.2f", totalCredit)}</td>
            <td></td>
        </tr>
        <tr class="total-row">
            <td colspan="4" style="text-align: ${if (isRtl) "left" else "right"};">$closingBalanceLabel</td>
            <td class="num">${String.format("%.2f", closingBalance)}</td>
        </tr>
    </tfoot>
    """.trimIndent()

        val html = generateHtmlShell(
            title = if (isRtl) "كشف حساب الموظف" else "Employee Ledger",
            subtitle = if (isRtl) employee.localizedName.arName
                ?: employee.name else employee.localizedName.enName ?: employee.name,
            dateRange = dateRangeText,
            isRtl = isRtl,
            headers = headers,
            rows = entryRows,
            footer = footer
        )

        return html
    }

    // Generic HTML template function to reduce code duplication
    private fun generateHtmlShell(
        title: String,
        subtitle: String,
        dateRange: String,
        isRtl: Boolean,
        headers: List<String>? = null,
        rows: String,
        footer: String? = null,
    ): String {
        val dir = if (isRtl) "rtl" else "ltr"
        val textAlign = if (isRtl) "right" else "left"
        // Use the font name in the CSS
        val fontFamily = if (isRtl) "Noto Sans Arabic" else "sans-serif"

        val headerCells = headers?.joinToString("") { header ->
            val style = if (headers.last() == header) "class=\"num-header\"" else ""
            "<th $style>$header</th>"
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: '$fontFamily', sans-serif; margin: 25px; direction: $dir; }
                h1, h2, p { text-align: center; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; text-align: $textAlign; }
                th { background-color: #f2f2f2; }
                .num { text-align: right; font-family: monospace; }
                .num-header { text-align: right; }
            </style>
        </head>
        <body>
            <h1>$title</h1>
            <h2>$subtitle</h2>
            <p>$dateRange</p>
            <table>
                ${if (headers != null) "<thead><tr>${headerCells ?: ""}</tr></thead>" else ""}
                <tbody>$rows</tbody>
                ${footer ?: ""}
            </table>
        </body>
        </html>
        """.trimIndent()
    }

}