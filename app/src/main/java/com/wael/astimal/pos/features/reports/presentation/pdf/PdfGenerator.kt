package com.wael.astimal.pos.features.reports.presentation.pdf

import android.content.Context
import android.graphics.Paint
import android.graphics.Typeface
import android.graphics.pdf.PdfDocument
import android.net.Uri
import androidx.core.content.FileProvider
import com.wael.astimal.pos.R
import com.wael.astimal.pos.features.management.domain.entity.BusinessPartner
import com.wael.astimal.pos.features.reports.domain.entity.AccountTransaction
import java.io.File
import java.io.FileOutputStream
import java.io.IOException
import java.time.format.DateTimeFormatter
import java.util.Locale

class PdfGenerator(private val context: Context) {

    private val dateFormatter = DateTimeFormatter.ofPattern("dd MMM yy", Locale.getDefault())

    fun generateStatementPdf(
        partner: BusinessPartner,
        transactions: List<AccountTransaction>
    ): Uri? {
        val document = PdfDocument()
        val pageHeight = 1120 // A4 paper height
        val pageWidth = 792  // A4 paper width
        var yPosition = 100

        // Create a page
        var pageInfo = PdfDocument.PageInfo.Builder(pageWidth, pageHeight, 1).create()
        var page = document.startPage(pageInfo)
        var canvas = page.canvas

        // --- Draw Header ---
        val titlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 24f
            color = android.graphics.Color.BLACK
        }
        val subtitlePaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 14f
            color = android.graphics.Color.DKGRAY
        }
        canvas.drawText(
            context.getString(R.string.account_statement),
            40f,
            yPosition.toFloat(),
            titlePaint
        )
        yPosition += 30
        canvas.drawText(partner.name.enName ?: "", 40f, yPosition.toFloat(), subtitlePaint)
        yPosition += 20
        partner.address?.let {
            canvas.drawText(it, 40f, yPosition.toFloat(), subtitlePaint)
        }
        yPosition += 50

        // --- Draw Table Header ---
        drawTableHeader(canvas, yPosition)
        yPosition += 30

        // --- Draw Table Rows ---
        val rowPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.NORMAL)
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        for (transaction in transactions) {
            // Check if we need a new page
            if (yPosition > pageHeight - 50) {
                document.finishPage(page)
                pageInfo =
                    PdfDocument.PageInfo.Builder(pageWidth, pageHeight, document.pages.size + 1)
                        .create()
                page = document.startPage(pageInfo)
                canvas = page.canvas
                yPosition = 60 // Reset Y for new page
                drawTableHeader(canvas, yPosition)
                yPosition += 30
            }
            drawTransactionRow(canvas, yPosition, transaction, rowPaint)
            yPosition += 25
        }

        // Finish the last page
        document.finishPage(page)

        // --- Save and Share ---
        return try {
            val file = File(context.cacheDir, "statement_${partner.name.enName}.pdf")
            document.writeTo(FileOutputStream(file))
            document.close()
            FileProvider.getUriForFile(context, "${context.packageName}.provider", file)
        } catch (e: IOException) {
            e.printStackTrace()
            null
        }
    }

    private fun drawTableHeader(canvas: android.graphics.Canvas, y: Int) {
        val headerPaint = Paint().apply {
            typeface = Typeface.create(Typeface.DEFAULT, Typeface.BOLD)
            textSize = 12f
            color = android.graphics.Color.BLACK
        }
        canvas.drawText(context.getString(R.string.date), 40f, y.toFloat(), headerPaint)
        canvas.drawText(context.getString(R.string.description), 140f, y.toFloat(), headerPaint)
        canvas.drawText(context.getString(R.string.debit), 450f, y.toFloat(), headerPaint)
        canvas.drawText(context.getString(R.string.credit), 550f, y.toFloat(), headerPaint)
        canvas.drawText(context.getString(R.string.balance), 670f, y.toFloat(), headerPaint)
    }

    private fun drawTransactionRow(
        canvas: android.graphics.Canvas,
        y: Int,
        transaction: AccountTransaction,
        paint: Paint
    ) {
        canvas.drawText(transaction.date.format(dateFormatter), 40f, y.toFloat(), paint)
        canvas.drawText(transaction.description, 140f, y.toFloat(), paint)
        if (transaction.debit != 0.0) {
            canvas.drawText(
                String.format(Locale.US, "%.2f", transaction.debit),
                450f,
                y.toFloat(),
                paint
            )
        }
        if (transaction.credit != 0.0) {
            canvas.drawText(
                String.format(Locale.US, "%.2f", transaction.credit),
                550f,
                y.toFloat(),
                paint
            )
        }
        canvas.drawText(
            String.format(Locale.US, "%.2f", transaction.balance),
            670f,
            y.toFloat(),
            paint
        )
    }
}
