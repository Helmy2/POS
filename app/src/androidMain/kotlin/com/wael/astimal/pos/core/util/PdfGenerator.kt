package com.wael.astimal.pos.core.util

import android.content.Context
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import androidx.core.content.FileProvider
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.data.entity.TransactionType
import com.wael.astimal.pos.features.management.domain.entity.AccountTransaction
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.lang.String.format
import java.time.format.DateTimeFormatter
import java.util.Locale

actual class PdfGeneratorImpl(private val context: Context) : PdfGenerator {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy", Locale.US)
    private val pageHeight = 1120
    private val pageWidth = 792
    private val margin = 40f

    // Define paints for different text styles
    private val titlePaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 22f
        color = Color.BLACK
    }
    private val partnerPaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 16f
        color = Color.BLACK
    }
    private val addressPaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
        textSize = 14f
        color = Color.DKGRAY
    }
    private val headerPaint = Paint().apply {
        typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
        textSize = 11f
        color = Color.BLACK
    }
    private val rowPaint = Paint().apply {
        typeface = Typeface.create(Typeface.MONOSPACE, Typeface.NORMAL)
        textSize = 11f
        color = Color.BLACK
    }
    private val debitPaint = Paint(rowPaint).apply { color = Color.RED }
    private val creditPaint = Paint(rowPaint).apply { color = Color.GREEN }

    override fun generateStatementPdf(
        partner: BusinessPartner,
        transactions: List<AccountTransaction>
    ) {
        val document = PdfDocument()

        // Start the first page
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // --- Draw Header ---
        var yPosition = drawReportHeader(canvas, partner)

        // --- Draw Table Header ---
        drawTableHeader(canvas, yPosition)
        yPosition += 40

        // --- Draw Table Rows ---
        for (transaction in transactions) {
            // Check if we need a new page before drawing the row
            if (yPosition > pageHeight - 50) {
                document.finishPage(page)
                pageInfo =
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1)
                        .create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = drawReportHeader(
                    canvas,
                    partner,
                    isContinuation = true
                ) // Redraw header on new page
                drawTableHeader(canvas, yPosition)
                yPosition += 25
            }
            drawTransactionRow(canvas, yPosition, transaction)
            yPosition += 20
        }

        // Finish the last page
        document.finishPage(page)

        // --- Save and return the URI ---
        // todo return
        try {
            val file =
                File(
                    context.cacheDir,
                    "statement_${partner.name.enName?.replace(" ", "_")}.pdf"
                )
            document.writeTo(FileOutputStream(file))
            document.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun drawReportHeader(
        canvas: android.graphics.Canvas,
        partner: BusinessPartner,
        isContinuation: Boolean = false
    ): Int {
        var y = margin.toInt() + 30
        canvas.drawText(
            context.getString(R.string.account_statement),
            margin,
            y.toFloat(),
            titlePaint
        )
        if (isContinuation) {
            val continuedPaint = Paint(addressPaint)
            canvas.drawText(
                "(continued...)",
                pageWidth - margin,
                y.toFloat(),
                continuedPaint.apply { textAlign = Paint.Align.RIGHT })
        }

        y += 40
        canvas.drawText(partner.name.enName ?: "", margin, y.toFloat(), partnerPaint)
        y += 20
        partner.address.let {
            canvas.drawText(it, margin, y.toFloat(), addressPaint)
        }
        y += 40
        return y
    }

    private fun drawTableHeader(canvas: android.graphics.Canvas, y: Int) {
        val yFloat = y.toFloat()
        canvas.drawLine(margin, yFloat - 5, pageWidth - margin, yFloat - 5, headerPaint)
        canvas.drawText(context.getString(R.string.date), margin, yFloat + 15, headerPaint)
        canvas.drawText(
            context.getString(R.string.description),
            margin + 140f,
            yFloat + 15,
            headerPaint
        )
        headerPaint.textAlign = Paint.Align.RIGHT
        canvas.drawText(
            context.getString(R.string.debit),
            pageWidth - margin - 240f,
            yFloat + 15,
            headerPaint
        )
        canvas.drawText(
            context.getString(R.string.credit),
            pageWidth - margin - 160f,
            yFloat + 15,
            headerPaint
        )
        canvas.drawText(
            context.getString(R.string.balance),
            pageWidth - margin - 40f,
            yFloat + 15,
            headerPaint
        )
        headerPaint.textAlign = Paint.Align.LEFT
        canvas.drawLine(margin, yFloat + 25, pageWidth - margin, yFloat + 25, headerPaint)
    }

    private fun drawTransactionRow(
        canvas: android.graphics.Canvas,
        y: Int,
        transaction: AccountTransaction
    ) {
        val yFloat = y.toFloat()
        val description = if (transaction.transactionType == TransactionType.OPENING_BALANCE) {
            context.getString(R.string.opening_balance)
        } else {
            "${getTransactionTypeString(transaction.transactionType)} #${transaction.invoiceNumber}"
        }

        canvas.drawText(transaction.date.format(dateFormatter), margin, yFloat, rowPaint)
        canvas.drawText(description, margin + 140f, yFloat, rowPaint)

        rowPaint.textAlign = Paint.Align.RIGHT
        debitPaint.textAlign = Paint.Align.RIGHT
        creditPaint.textAlign = Paint.Align.RIGHT

        if (transaction.debit != 0.0) {
            canvas.drawText(
                format(Locale.US, "%.2f", transaction.debit),
                pageWidth - margin - 240f,
                yFloat,
                debitPaint
            )
        }
        if (transaction.credit != 0.0) {
            canvas.drawText(
                format(Locale.US, "%.2f", transaction.credit),
                pageWidth - margin - 160f,
                yFloat,
                creditPaint
            )
        }
        canvas.drawText(
            format(Locale.US, "%.2f", transaction.balance),
            pageWidth - margin - 40f,
            yFloat,
            rowPaint.apply { isFakeBoldText = true })

        rowPaint.textAlign = Paint.Align.LEFT // Reset alignment
    }

    private fun getTransactionTypeString(type: TransactionType): String {
        return when (type) {
            TransactionType.OPENING_BALANCE -> context.getString(R.string.opening_balance)
            TransactionType.SALE -> context.getString(R.string.sale)
            TransactionType.PURCHASE -> context.getString(R.string.purchase)
            TransactionType.SALE_RETURN -> context.getString(R.string.sale_return)
            TransactionType.PURCHASE_RETURN -> context.getString(R.string.purchase_return)
            TransactionType.PAYMENT_RECEIVED -> context.getString(R.string.payment_received)
            TransactionType.PAYMENT_SENT -> context.getString(R.string.payment_sent)
        }
    }
}