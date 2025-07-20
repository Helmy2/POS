package com.wael.astimal.pos.core.util


import com.wael.astimal.pos.features.management.data.local.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.management.domain.entity.EmployeeTransaction
import com.wael.astimal.pos.features.reports.domain.model.DetailedTransaction
import com.wael.astimal.pos.features.reports.domain.model.EmployeeActivity
import com.wael.astimal.pos.features.reports.domain.model.EmployeeProfitSummary
import com.wael.astimal.pos.features.user.domain.entity.User
import kotlinx.datetime.Instant
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atStartOfDayIn
import kotlinx.datetime.toJavaInstant
import kotlinx.datetime.toLocalDateTime
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

    fun createEmployeeReportHtml(
        employee: User,
        transactions: List<EmployeeTransaction>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val transactionRows = transactions.joinToString("") { trx ->
            // Convert the Long timestamp from the transaction to a readable date
            val transactionDate = Instant.fromEpochMilliseconds(trx.createdAt)
                .toLocalDateTime(TimeZone.UTC).date
            """
            <tr>
                <td>${formatDate(transactionDate)}</td>
                <td>${trx.notes ?: trx.type.name.replace('_', ' ')}</td>
                <td class="num">${trx.amount}</td>
            </tr>
            """.trimIndent()
        }

        val formattedStartDate = formatDate(startDate)
        val formattedEndDate = formatDate(endDate)
        val dateRangeText = if (formattedStartDate == formattedEndDate) {
            "Report for date: $formattedStartDate"
        } else {
            "Report for period: $formattedStartDate to $formattedEndDate"
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
                <h1>Employee Report</h1>
                <h2>${employee.name}</h2>
                <p class="date-range">$dateRangeText</p>
            </div>
            <table>
                <thead>
                    <tr>
                        <th>Date</th>
                        <th>Description</th>
                        <th class="num">Total Amount</th>
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

    fun createEmployeeActivityReportHtml(
        employee: User,
        activities: List<EmployeeActivity>,
        startDate: LocalDate,
        endDate: LocalDate
    ): String {
        val activityRows = activities.joinToString("") { activity ->
            val date = activity.timestamp.toLocalDateTime(TimeZone.UTC).date
            """
        <tr>
            <td>${formatDate(date)}</td>
            <td>${activity.type}</td>
            <td>${activity.details}</td>
            <td class="num">${String.format("%.2f", activity.amount)}</td>
        </tr>
        """.trimIndent()
        }

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
            h1, h2, p { text-align: center; }
            table { width: 100%; border-collapse: collapse; margin-top: 20px; }
            th, td { border: 1px solid #ddd; padding: 8px; font-size: 12px; }
            th { background-color: #f2f2f2; text-align: left; }
            .num { text-align: right; font-family: monospace; }
        </style>
    </head>
    <body>
        <h1>Employee Activity Report</h1>
        <h2>${employee.name}</h2>
        <p>$dateRangeText</p>
        <table>
            <thead>
                <tr>
                    <th>Date</th>
                    <th>Type</th>
                    <th>Details</th>
                    <th class="num">Amount</th>
                </tr>
            </thead>
            <tbody>
                $activityRows
            </tbody>
        </table>
    </body>
    </html>
    """.trimIndent()
    }
}