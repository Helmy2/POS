package com.wael.astimal.pos.core.util


import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import java.text.SimpleDateFormat
import java.time.format.DateTimeFormatter
import java.util.Date
import java.util.Locale

/**
 * Generates a self-contained HTML string for reports.
 * This is the single source of truth for the report's layout and styling.
 */
class HtmlReportGenerator {

    private fun formatDate(date: LocalDate): String {
        val sdf = SimpleDateFormat("dd MMM yyyy", Locale.US)
        // Convert LocalDate to a format SimpleDateFormat can use
        val dateInMillis = date.atStartOfDayIn(TimeZone.UTC).toJavaInstant().toEpochMilli()
        return sdf.format(Date(dateInMillis))
    }

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault())

    fun createStatementHtml(
        partner: BusinessPartner,
        transactions: List<AccountTransaction>
    ): String {
        val transactionRows = transactions.joinToString("") { trx ->
            val description = if (trx.transactionType == TransactionType.OPENING_BALANCE) {
                "Opening Balance"
            } else {
                trx.transactionType.name.replace('_', ' ')
            }

            """
            <tr>
                <td>${dateFormatter.format(trx.date)}</td>
                <td>$description</td>
                <td class="num debit">${formatCurrency(trx.debit)}</td>
                <td class="num credit">${formatCurrency(trx.credit)}</td>
                <td class="num balance">${formatCurrency(trx.balance)}</td>
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
                p { font-size: 14px; color: #333; }
                table { width: 100%; border-collapse: collapse; margin-top: 20px; }
                th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }
                th { background-color: #f2f2f2; text-align: left; }
                .num { text-align: right; font-family: monospace; }
                .debit { color: #D32F2F; }
                .credit { color: #388E3C; }
                .balance { font-weight: bold; }
                .header { margin-bottom: 30px; }
            </style>
        </head>
        <body>
            <div class="header">
                <h1>Account Statement</h1>
                <h2>${partner.name.enName}</h2>
                ${partner.address.let { "<p>${it}</p>" }}
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th class="num">Debit</th>
                        <th class="num">Credit</th>
                        <th class="num">Balance</th>
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

    private fun formatCurrency(value: Double): String {
        return if (value == 0.0) "" else String.format(Locale.US, "%.2f", value)
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
}