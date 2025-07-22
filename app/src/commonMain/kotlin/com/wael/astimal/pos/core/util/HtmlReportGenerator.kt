package com.wael.astimal.pos.core.util


import com.wael.astimal.pos.core.domain.entity.Language
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.model.EmployeeProfitSummary
import com.wael.astimal.pos.features.user.data.local.SettingsManager
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.coroutines.flow.first
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

/**
 * Generates a self-contained HTML string for reports.
 * This is the single source of truth for the report's layout and styling.
 */
class HtmlReportGenerator(
    val settingsManager: SettingsManager
) {

    private fun formatDate(date: LocalDate): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        // Convert LocalDate to a format SimpleDateFormat can use
        val dateInMillis = date.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        return sdf.format(Date(dateInMillis))
    }

    fun createCustomerStatementHtml(
        partner: BusinessPartner,
        transactions: List<DetailedTransaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val transactionRows = transactions.joinToString("") { trx ->
            """
            <tr>
                <td>${formatDate(trx.date)}</td>
                <td>${trx.description}</td>
                <td class="num">${trx.totalAmount}</td>
            </tr>
            """.trimIndent()
        }

        return """
        <!DOCTYPE html>
        <html>
        <head>
            <meta charset="UTF-8">
            <style>
                body { font-family: sans-serif; margin: 25px; }
                h1 { font-size: 24px; }
                h2 { font-size: 18px; }
                p { font-size: 14px; color: #555; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }
                th { background-color: #f2f2f2; text-align: left; }
                .num { text-align: right; font-family: monospace; }
                .header { margin-bottom: 30px; }
                .date-range { font-size: 12px; color: #777; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Customer Statement</h1>
                <h2>${partner.name.enName}</h2>
                <p class="date-range">Report for period: ${formatDate(startDate)} to ${
            formatDate(
                endDate
            )
        }</p>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th class="num">Amount</th>
                    </tr>
                </thead>
                <tbody>
                    $transactionRows
                </tbody>
            </table>
        </body>
        </html>
        """.trimIndent()
    }

    @Suppress("DefaultLocale")
    fun createProfitReportHtml(
        employee: User,
        summary: List<EmployeeProfitSummary>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val summaryRows = summary.joinToString("") {
            """
        <tr>
            <td>${formatDate(it.date)}</td>
            <td class="num">${String.format("%.2f", it.directCommission)}</td>
            <td class="num">${String.format("%.2f", it.responsibilityCommission)}</td>
            <td class="num">${String.format("%.2f", it.totalCommission)}</td>
        </tr>
        """.trimIndent()
        }
        val totalDirect = summary.sumOf { it.directCommission }
        val totalResponsibility = summary.sumOf { it.responsibilityCommission }
        val grandTotal = summary.sumOf { it.totalCommission }

        val dateRangeText = if (formatDate(startDate) == formatDate(endDate)) {
            "Report for date: ${formatDate(startDate)}"
        } else {
            "Report for period: ${formatDate(startDate)} to ${formatDate(endDate)}"
        }

        return """
    <!DOCTYPE html>
    <html>
    <head>
        <meta charset="UTF-8">
        <style>
            body { font-family: sans-serif; margin: 25px; }
            h1, h2 { text-align: center; }
            p { font-size: 14px; color: #555; text-align: center; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }
            th { background-color: #f2f2f2; text-align: center; }
            .num { text-align: right; font-family: monospace; }
            .total-row { background-color: #f2f2f2; font-weight: bold; }
        </style>
    </head>
    <body>
        <h1>Profits Report</h1>
        <h2>${employee.name}</h2>
        <p>$dateRangeText</p>
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th class="num">Direct Commission</th>
                    <th class="num">Responsibility Commission</th>
                    <th class="num">Total Commission</th>
                </tr>
            </thead>
            <tbody>
                $summaryRows
            </tbody>
            <tfoot>
                <tr class="total-row">
                    <td>TOTAL</td>
                    <td class="num">${String.format("%.2f", totalDirect)}</td>
                    <td class="num">${String.format("%.2f", totalResponsibility)}</td>
                    <td class="num">${String.format("%.2f", grandTotal)}</td>
                </tr>
            </tfoot>
        </table>
    </body>
    </html>
    """.trimIndent()
    }


    suspend fun createEmployeeActivityReportHtml(
        employee: User,
        activities: List<EmployeeActivity>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val lang = settingsManager.getLanguage().first()
        val isRtl = lang == Language.Arabic

        val activityRows = activities.joinToString("") { activity ->
            val date = activity.timestamp.toLocalDateTime(TimeZone.UTC).date

            // Determine type and details based on the selected language
            val typeText: String
            val detailsText: String
            val amount: Double

            when (activity) {
                is EmployeeActivity.InvoiceActivity -> {
                    typeText = activity.invoice.invoiceType.name.replace('_', ' ')
                    detailsText = if (isRtl) {
                        "فاتورة #${activity.invoice.id} إلى ${activity.invoice.partner.name.arName}"
                    } else {
                        "Invoice #${activity.invoice.id} to ${activity.invoice.partner.name.enName}"
                    }
                    amount = activity.invoice.totalAmount
                }

                is EmployeeActivity.FinancialActivity -> {
                    typeText = activity.transaction.type.name.replace('_', ' ')
                    detailsText = activity.transaction.notes ?: typeText
                    amount = activity.transaction.amount
                }

                is EmployeeActivity.PartnerPaymentActivity -> {
                    typeText = if (isRtl) "دفعة شريك" else "Partner Payment"
                    detailsText = if (isRtl) {
                        "دفعة من/إلى ${activity.transaction.partner.name.arName}"
                    } else {
                        "Payment from/to ${activity.transaction.partner.name.enName}"
                    }
                    amount = activity.transaction.amount
                }
            }

            """
        <tr>
            <td>${formatDate(date)}</td>
            <td>$typeText</td>
            <td>$detailsText</td>
            <td class="num">${String.format("%.2f", amount)}</td>
        </tr>
        """.trimIndent()
        }

        val dateRangeText = if (formatDate(startDate) == formatDate(endDate)) {
            if (isRtl) "تقرير لتاريخ: ${formatDate(startDate)}" else "Report for date: ${
                formatDate(
                    startDate
                )
            }"
        } else {
            if (isRtl) "تقرير للفترة: ${formatDate(startDate)} إلى ${formatDate(endDate)}" else "Report for period: ${
                formatDate(
                    startDate
                )
            } to ${formatDate(endDate)}"
        }

        val title = if (isRtl) "تقرير نشاط الموظف" else "Employee Activity Report"
        val headers = if (isRtl) {
            listOf("التاريخ", "النوع", "التفاصيل", "المبلغ")
        } else {
            listOf("Date", "Type", "Details", "Amount")
        }

        return generateHtmlShell(
            title = title,
            subtitle = employee.name,
            dateRange = dateRangeText,
            isRtl = isRtl,
            headers = headers,
            rows = activityRows
        )
    }

    // Generic HTML template function to reduce code duplication
    private fun generateHtmlShell(
        title: String,
        subtitle: String,
        dateRange: String,
        isRtl: Boolean,
        headers: List<String>,
        rows: String,
        footer: String? = null
    ): String {
        val dir = if (isRtl) "rtl" else "ltr"
        val textAlign = if (isRtl) "right" else "left"
        // Use the font name in the CSS
        val fontFamily = if (isRtl) "Noto Sans Arabic" else "sans-serif"

        val headerCells = headers.joinToString("") { header ->
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
                <thead><tr>$headerCells</tr></thead>
                <tbody>$rows</tbody>
                ${footer ?: ""}
            </table>
        </body>
        </html>
        """.trimIndent()
    }

}